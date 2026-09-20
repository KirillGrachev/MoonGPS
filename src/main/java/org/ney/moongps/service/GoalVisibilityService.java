package org.ney.moongps.service;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;

import java.util.Collection;
import java.util.List;

/**
 * Сервис видимости меток.
 * При включённом ограничении метки чужих миров скрыты от игрока,
 * пока у него нет отдельного права. Консоль видит все метки.
 */
public class GoalVisibilityService {

    private final ConfigManager configManager;
    private final PermissionService permissionService;

    public GoalVisibilityService(@NotNull ConfigManager configManager,
                                 @NotNull PermissionService permissionService) {

        this.configManager = configManager;
        this.permissionService = permissionService;

    }

    /**
     * Проверяет, видна ли метка отправителю.
     *
     * @param viewer отправитель (null или консоль - видно всё)
     * @param goal   метка
     * @return true если метку можно показывать и использовать
     */
    public boolean isVisible(@Nullable CommandSender viewer, @NotNull GPSGoal goal) {

        if (!(viewer instanceof Player player)) return true;
        if (!configManager.isOtherWorldRestricted()) return true;
        if (goal.world().equals(player.getWorld().getName())) return true;

        return permissionService.hasPermission(player, configManager.getOtherWorldPermission());

    }

    /**
     * Оставляет только видимые для отправителя метки.
     *
     * @param viewer отправитель
     * @param goals  исходные метки
     * @return видимые метки
     */
    public @NotNull List<GPSGoal> filterVisible(@Nullable CommandSender viewer,
                                                 @NotNull Collection<GPSGoal> goals) {

        return goals.stream()
                .filter(goal -> isVisible(viewer, goal))
                .toList();

    }
}
