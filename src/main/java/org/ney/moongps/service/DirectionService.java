package org.ney.moongps.service;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.type.DirectionMode;
import org.ney.moongps.config.type.DirectionSettings;
import org.ney.moongps.model.Direction;

import java.util.Map;

/**
 * Сервис расчёта направления от игрока до метки.
 * Вся математика выполняется без обращений к конфигу - настройки кэшируются.
 */
public class DirectionService {

    private static final double MIN_VECTOR_LENGTH = 1.0E-4D;
    private static final double COMPASS_SECTOR = 45.0D;
    private static final double FULL_CIRCLE = 360.0D;

    private final ConfigManager configManager;

    public DirectionService(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Рассчитывает расстояние от игрока до метки.
     *
     * @param playerLocation локация игрока
     * @param goalLocation   локация метки
     * @return расстояние в блоках
     */
    public double calculateDistance(@NotNull Location playerLocation, @NotNull Location goalLocation) {
        return playerLocation.distance(goalLocation);
    }

    /**
     * Рассчитывает направление от игрока до метки.
     *
     * @param playerLocation локация игрока
     * @param goalLocation   локация метки
     * @return направление (относительное и по компасу)
     */
    public @NotNull Direction calculateDirection(@NotNull Location playerLocation, @NotNull Location goalLocation) {

        Vector toGoal = goalLocation.toVector().subtract(playerLocation.toVector());

        Direction.Relative relative = calculateRelative(playerLocation, toGoal);
        Direction.Compass compass = calculateCompass(playerLocation, goalLocation);

        return new Direction(relative, compass);

    }

    /**
     * Определяет направление относительно взгляда игрока.
     * Скалярное произведение показывает, впереди метка или позади,
     * а знак Y-компоненты векторного произведения - сторону:
     * в системе координат Minecraft (север -Z, восток +X) положительный
     * знак означает, что метка находится слева от игрока.
     *
     * @param playerLocation локация игрока
     * @param toGoal         вектор от игрока к метке
     * @return относительное направление
     */
    private @NotNull Direction.Relative calculateRelative(@NotNull Location playerLocation,
                                                          @NotNull Vector toGoal) {

        DirectionSettings settings = configManager.getDirectionSettings();

        Vector lookDirection = playerLocation.getDirection().setY(0);
        Vector goalDirection = toGoal.clone().setY(0);

        if (lookDirection.lengthSquared() < MIN_VECTOR_LENGTH
                || goalDirection.lengthSquared() < MIN_VECTOR_LENGTH) {
            return Direction.Relative.AHEAD;
        }

        lookDirection.normalize();
        goalDirection.normalize();

        double dotProduct = lookDirection.dot(goalDirection);

        if (dotProduct >= settings.aheadThreshold()) {
            return Direction.Relative.AHEAD;
        }

        if (dotProduct <= settings.behindThreshold()) {
            return Direction.Relative.BEHIND;
        }

        double crossProductY = lookDirection.getCrossProduct(goalDirection).getY();

        return crossProductY > 0 ? Direction.Relative.LEFT : Direction.Relative.RIGHT;

    }

    /**
     * Определяет сторону света от игрока к метке.
     * В Minecraft север - это -Z, восток - +X, а atan2(deltaX, -deltaZ)
     * сразу даёт угол по часовой стрелке от севера.
     *
     * @param playerLocation локация игрока
     * @param goalLocation   локация метки
     * @return сторона света
     */
    private @NotNull Direction.Compass calculateCompass(@NotNull Location playerLocation,
                                                        @NotNull Location goalLocation) {

        double deltaX = goalLocation.getX() - playerLocation.getX();
        double deltaZ = goalLocation.getZ() - playerLocation.getZ();

        if (Math.abs(deltaX) < MIN_VECTOR_LENGTH && Math.abs(deltaZ) < MIN_VECTOR_LENGTH) {
            return Direction.Compass.NORTH;
        }

        double angle = Math.toDegrees(Math.atan2(deltaX, -deltaZ));
        double normalized = ((angle % FULL_CIRCLE) + FULL_CIRCLE) % FULL_CIRCLE;
        int index = (int) Math.floor((normalized + COMPASS_SECTOR / 2) / COMPASS_SECTOR) % 8;

        return Direction.Compass.values()[index];

    }

    /**
     * Оформляет направление текстом из конфига.
     * Если в сообщении есть плейсхолдеры {arrow} или {name} - применяется
     * формат settings.direction.format, иначе сообщение используется как есть.
     *
     * @param direction рассчитанное направление
     * @return цветная строка направления
     */
    public @NotNull String format(@NotNull Direction direction) {

        DirectionSettings settings = configManager.getDirectionSettings();

        if (settings.mode() == DirectionMode.COMPASS) {
            return formatCompass(settings, direction);
        }

        return formatRelative(settings, direction);

    }

    private @NotNull String formatCompass(@NotNull DirectionSettings settings, @NotNull Direction direction) {

        String key = direction.compass().name();
        String arrow = direction.compass().getArrow();
        String name = symbolByKey(settings.compassSymbols(), key, arrow);

        return applyFormat(settings.format(), arrow, name);

    }

    private @NotNull String formatRelative(@NotNull DirectionSettings settings, @NotNull Direction direction) {

        String key = direction.relative().name();
        String arrow = arrowOf(direction.relative());
        String name = symbolByKey(settings.relativeNames(), key, arrow);

        return applyFormat(settings.format(), arrow, name);

    }

    private @NotNull String symbolByKey(@NotNull Map<String, String> symbols,
                                        @NotNull String key,
                                        @NotNull String fallback) {
        return symbols.getOrDefault(key, fallback);
    }

    private @NotNull String applyFormat(@NotNull String format, @NotNull String arrow, @NotNull String name) {

        if (!format.contains("{arrow}") && !format.contains("{name}")) {
            return name;
        }
        return format
                .replace("{arrow}", arrow)
                .replace("{name}", name);

    }

    private @NotNull String arrowOf(@NotNull Direction.Relative relative) {

        return switch (relative) {
            case AHEAD -> "↑";
            case LEFT -> "←";
            case RIGHT -> "→";
            case BEHIND -> "↓";
        };

    }
}
