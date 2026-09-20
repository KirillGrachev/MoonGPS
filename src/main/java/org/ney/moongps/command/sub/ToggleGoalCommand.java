package org.ney.moongps.command.sub;

import org.jetbrains.annotations.NotNull;
import org.ney.moongps.command.CommandContext;
import org.ney.moongps.command.GpsSubCommand;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.service.NavigationService;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.util.Placeholders;

/**
 * Подкоманда по умолчанию: /gps <метка>.
 * Включает навигатор до метки, а при повторном вызове - выключает его.
 * Без аргументов выводит подсказку об использовании.
 */
public class ToggleGoalCommand implements GpsSubCommand {

    private final ConfigManager configManager;
    private final NavigationService navigationService;
    private final MessageService messageService;

    public ToggleGoalCommand(@NotNull ConfigManager configManager,
                             @NotNull NavigationService navigationService,
                             @NotNull MessageService messageService) {

        this.configManager = configManager;
        this.navigationService = navigationService;
        this.messageService = messageService;

    }

    /**
     * Служебное имя действия по умолчанию: маршрутизуется только
     * когда первый аргумент не совпал ни с одной подкомандой.
     */
    @Override
    public @NotNull String getName() {
        return "";
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public String getPermission() {
        return configManager.getPermissionUse();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        if (args.length == 0) {

            GPSGoal activeGoal = navigationService.getActiveGoal(context.requirePlayer());

            if (activeGoal == null) {

                messageService.send(context.sender(), configManager.getUsageMessage(), Placeholders.create());
                return true;

            }

            navigationService.stopNavigation(context.requirePlayer(), true);

            return true;

        }

        navigationService.toggleGoal(context.requirePlayer(), args[0], true);

        return true;

    }
}
