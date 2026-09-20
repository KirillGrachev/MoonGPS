package org.ney.moongps.service;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ney.moongps.config.ConfigManager;

/**
 * Сервис проверки прав доступа.
 * Если система прав выключена в конфиге - проверка проходит для всех,
 * иначе используются права из plugin.yml.
 */
public class PermissionService {

    private final ConfigManager configManager;

    public PermissionService(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Проверяет наличие права у отправителя.
     *
     * @param sender     отправитель
     * @param permission название права (null - право не требуется)
     * @return true если доступ разрешён
     */
    public boolean hasPermission(@NotNull CommandSender sender, @Nullable String permission) {

        if (!configManager.arePermissionsEnabled()) return true;
        if (permission == null || permission.isBlank()) return true;
        if (isOpBypass(sender)) return true;

        return sender.hasPermission(permission);

    }

    /**
     * Проверяет обход прав операторами и консолью.
     *
     * @param sender отправитель
     * @return true если проверка прав для отправителя не нужна
     */
    private boolean isOpBypass(@NotNull CommandSender sender) {

        if (!configManager.isOpBypassEnabled()) return false;
        return sender.isOp() || sender instanceof ConsoleCommandSender;

    }
}
