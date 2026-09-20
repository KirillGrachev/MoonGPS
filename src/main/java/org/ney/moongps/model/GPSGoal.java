package org.ney.moongps.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Метка навигатора: название, координаты и мир.
 *
 * @param name       название метки
 * @param x          координата X
 * @param y          координата Y
 * @param z          координата Z
 * @param world      название мира
 * @param permission право доступа к метке (null - доступно всем)
 */
public record GPSGoal(@NotNull String name,
                      double x,
                      double y,
                      double z,
                      @NotNull String world,
                      @Nullable String permission) {

    /**
     * Создаёт метку по названию, локации и праву доступа.
     *
     * @param name       название метки
     * @param location   локация метки
     * @param permission право доступа (может быть null)
     * @return новая метка
     */
    public static @NotNull GPSGoal of(@NotNull String name,
                                      @NotNull Location location,
                                      @Nullable String permission) {

        return new GPSGoal(
                name,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getWorld() != null ? location.getWorld().getName() : "world",
                permission
        );

    }

    /**
     * Собирает локацию метки.
     *
     * @return локация или null, если мир метки не загружен
     */
    public @Nullable Location toLocation() {

        World goalWorld = Bukkit.getWorld(world);
        if (goalWorld == null) return null;

        return new Location(goalWorld, x, y, z);

    }

    /**
     * Проверяет, загружен ли мир метки.
     *
     * @return true если мир доступен
     */
    public boolean isWorldLoaded() {
        return Bukkit.getWorld(world) != null;
    }
}
