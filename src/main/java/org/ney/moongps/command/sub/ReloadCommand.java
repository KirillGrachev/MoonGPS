package org.ney.moongps.command.sub;

import org.jetbrains.annotations.NotNull;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.command.CommandContext;
import org.ney.moongps.command.GpsSubCommand;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.NavigationService;
import org.ney.moongps.util.Placeholders;

/**
 * Подкоманда /gps reload.
 * Перезагружает config.yml, goals.yml и активные сессии навигации.
 */
public class ReloadCommand implements GpsSubCommand {

    private final MoonGPS plugin;
    private final ConfigManager configManager;
    private final MessageService messageService;

    public ReloadCommand(@NotNull MoonGPS plugin,
                         @NotNull ConfigManager configManager,
                         @NotNull MessageService messageService) {

        this.plugin = plugin;
        this.configManager = configManager;
        this.messageService = messageService;

    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return configManager.getPermissionReload();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        int marksCount = plugin.reloadPlugin();
        NavigationService navigationService = plugin.getNavigationService();

        messageService.send(context.sender(), configManager.getReloadSuccessMessage(),
                Placeholders.create()
                        .add("marks", marksCount)
                        .add("players", navigationService == null ? 0 : navigationService.getActiveSessionsCount())
        );

        return true;

    }
}
