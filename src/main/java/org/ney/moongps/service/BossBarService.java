package org.ney.moongps.service;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.type.BossBarProgress;
import org.ney.moongps.config.type.DisplaySettings;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис boss bar навигации.
 * У каждого игрока своя полоса: текст направления и заполнение
 * по мере приближения к метке. Все методы вызываются из главного потока.
 */
public class BossBarService {

    private static final double MIN_PROGRESS = 0.02D;
    private static final double MAX_PROGRESS = 1.0D;

    private final MoonGPS plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BarHolder> activeBars = new ConcurrentHashMap<>();

    public BossBarService(@NotNull MoonGPS plugin, @NotNull ConfigManager configManager) {

        this.plugin = plugin;
        this.configManager = configManager;

    }

    /**
     * Показывает или обновляет полосу игрока.
     *
     * @param player   игрок
     * @param text     готовый текст полосы
     * @param distance текущее расстояние до метки
     */
    public void update(@NotNull Player player, @NotNull String text, double distance) {

        BarHolder holder = activeBars.computeIfAbsent(player.getUniqueId(),
                uuid -> new BarHolder(createBar(player), distance)
        );

        holder.bar.setTitle(text);
        holder.bar.setProgress(calculateProgress(holder, distance));

    }

    /**
     * Показывает временную полосу (достижение метки) и снимает её через заданное время.
     *
     * @param player игрок
     * @param text   текст полосы
     * @param color  цвет
     * @param style  стиль
     * @param ticks  время показа в тиках
     */
    public void flash(@NotNull Player player,
                      @NotNull String text,
                      @NotNull BarColor color,
                      @NotNull BarStyle style,
                      long ticks) {

        BossBar bar = Bukkit.createBossBar(text, color, style);

        bar.setProgress(MAX_PROGRESS);
        bar.addPlayer(player);
        bar.setVisible(true);

        plugin.getServer().getScheduler().runTaskLater(plugin, bar::removeAll, ticks);

    }

    /**
     * Снимает полосу игрока.
     *
     * @param playerUUID игрок
     */
    public void remove(@NotNull UUID playerUUID) {

        BarHolder holder = activeBars.remove(playerUUID);
        if (holder != null) {
            holder.bar.removeAll();
        }

    }

    /**
     * Снимает все полосы (при выключении плагина).
     */
    public void removeAll() {

        activeBars.values().forEach(holder -> holder.bar.removeAll());
        activeBars.clear();

    }

    public int getActiveBarsCount() {
        return activeBars.size();
    }

    private @NotNull BossBar createBar(@NotNull Player player) {

        DisplaySettings settings = configManager.getDisplaySettings();
        BossBar bar = Bukkit.createBossBar("", settings.barColor(), settings.barStyle());

        bar.addPlayer(player);
        bar.setVisible(true);

        return bar;

    }

    private double calculateProgress(@NotNull BarHolder holder, double distance) {

        if (configManager.getDisplaySettings().progress() == BossBarProgress.FULL) {
            return MAX_PROGRESS;
        }

        if (holder.initialDistance <= 0.0D) {
            return MAX_PROGRESS;
        }

        double progress = 1.0D - distance / holder.initialDistance;

        return Math.max(MIN_PROGRESS, Math.min(MAX_PROGRESS, progress));

    }

    /**
     * Полоса игрока вместе с расстоянием на момент включения навигатора.
     */
    private static final class BarHolder {

        private final BossBar bar;
        private final double initialDistance;

        private BarHolder(@NotNull BossBar bar, double initialDistance) {

            this.bar = bar;
            this.initialDistance = initialDistance;

        }
    }
}
