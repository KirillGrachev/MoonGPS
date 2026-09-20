package org.ney.moongps.command.sub;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.command.CommandContext;
import org.ney.moongps.command.GpsSubCommand;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.registry.GoalRegistry;
import org.ney.moongps.registry.GoalStorage;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.NavigationService;
import org.ney.moongps.util.GoalNameValidator;

import java.util.List;
import org.ney.moongps.util.Placeholders;

/**
 * Подкоманда /gps set <название> [право].
 * Создаёт метку по текущей позиции игрока и сохраняет её в goals.yml.
 */
public class SetGoalCommand implements GpsSubCommand {

    private final ConfigManager configManager;
    private final GoalRegistry goalRegistry;
    private final GoalStorage goalStorage;
    private final NavigationService navigationService;
    private final MessageService messageService;

    public SetGoalCommand(@NotNull ConfigManager configManager,
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
        return "set";
    }

    @Override
    public String getPermission() {
        return configManager.getPermissionSet();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        if (args.length < 1) {

            messageService.send(context.sender(), configManager.getUsageMessage(), Placeholders.create());
            return true;

        }

        String goalName = args[0];

        if (!GoalNameValidator.isValid(goalName)) {

            messageService.send(context.sender(), configManager.getSetInvalidNameMessage(),
                    Placeholders.create().add("gps", goalName)
            );
            return true;

        }

        String permission = args.length > 1 ? args[1] : null;
        boolean goalExists = goalRegistry.isGoalRegistered(goalName);

        GPSGoal goal = GPSGoal.of(goalName, centerLocation(context.requirePlayer().getLocation()), permission);

        goalRegistry.registerGoal(goal);
        goalStorage.saveGoal(goal);

        // Игроки, идущие к старой версии метки, продолжают путь к новым координатам
        navigationService.refreshGoalForEveryone(goal.name());

        messageService.send(context.sender(),
                goalExists ? configManager.getSetUpdatedMessage() : configManager.getSetSuccessMessage(),
                goalPlaceholders(goal)
        );

        return true;

    }

    /**
     * Дополняет аргументы по позиции курсора: сначала название метки,
     * затем право. Новое имя вводится вручную - дополнять нечего.
     */
    @Override
    public @NotNull List<String> complete(@NotNull CommandContext context, String @NotNull [] args) {

        if (args.length == 1) return goalRegistry.getSortedGoalNames();
        if (args.length == 2) return goalRegistry.getUsedPermissions();

        return List.of();

    }

    private @NotNull Location centerLocation(@NotNull Location location) {

        Location centered = location.clone();

        centered.setX(Math.floor(centered.getX()) + 0.5D);
        centered.setZ(Math.floor(centered.getZ()) + 0.5D);

        return centered;

    }

    private @NotNull Placeholders goalPlaceholders(@NotNull GPSGoal goal) {

        return Placeholders.create()
                .add("gps", goal.name())
                .add("world", goal.world())
                .add("x", String.format("%.1f", goal.x()))
                .add("y", String.format("%.1f", goal.y()))
                .add("z", String.format("%.1f", goal.z()))
                .add("permission", goal.permission() == null ? "-" : goal.permission());

    }
}
