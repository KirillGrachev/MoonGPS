package org.ney.moongps.command.sub;

import org.jetbrains.annotations.NotNull;
import org.ney.moongps.command.CommandContext;
import org.ney.moongps.command.GpsSubCommand;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.registry.GoalRegistry;
import org.ney.moongps.registry.GoalStorage;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.NavigationService;
import org.ney.moongps.util.Placeholders;

import java.util.List;

/**
 * Подкоманда /gps delete <название>.
 * Удаляет метку из реестра, из goals.yml и останавливает навигатор у всех,
 * кто шёл к этой метке.
 */
public class DeleteGoalCommand implements GpsSubCommand {

    private final ConfigManager configManager;
    private final GoalRegistry goalRegistry;
    private final GoalStorage goalStorage;
    private final NavigationService navigationService;
    private final MessageService messageService;

    public DeleteGoalCommand(@NotNull ConfigManager configManager,
                             @NotNull GoalRegistry goalRegistry,
                             @NotNull GoalStorage goalStorage,
                             @NotNull NavigationService navigationService,
                             @NotNull MessageService messageService) {

        this.configManager = configManager;
        this.goalRegistry = goalRegistry;
        this.goalStorage = goalStorage;
        this.navigationService = navigationService;
        this.messageService = messageService;

    }

    @Override
    public @NotNull String getName() {
        return "delete";
    }

    @Override
    public String getPermission() {
        return configManager.getPermissionDelete();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        if (args.length < 1) {

            messageService.send(context.sender(), configManager.getUsageMessage(), Placeholders.create());
            return true;

        }

        String goalName = args[0];
        GPSGoal goal = goalRegistry.getGoal(goalName);

        if (goal == null) {

            messageService.send(context.sender(), configManager.getMarkNotFoundMessage(),
                    Placeholders.create().add("gps", goalName)
            );
            return true;

        }

        navigationService.stopGoalForEveryone(goal.name());

        goalRegistry.removeGoal(goal.name());
        goalStorage.deleteGoal(goal.name());

        messageService.send(context.sender(), configManager.getDeleteSuccessMessage(),
                Placeholders.create()
                        .add("gps", goal.name())
                        .add("world", goal.world())
        );

        return true;

    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandContext context, String @NotNull [] args) {

        if (args.length != 1) return List.of();
        return goalRegistry.getSortedGoalNames();

    }
}
