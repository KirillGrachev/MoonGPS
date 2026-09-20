package org.ney.moongps.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.service.GoalVisibilityService;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.PermissionService;
import org.ney.moongps.util.Placeholders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Обработчик команды /gps.
 * Без аргументов выводит справку, с аргументом - передаёт управление
 * подкоманде или включает навигатор до метки.
 */
public class GpsCommand implements TabExecutor {

    private static final String COMMAND_NAME = "gps";

    private final ConfigManager configManager;
    private final MessageService messageService;
    private final PermissionService permissionService;
    private final GpsSubCommand defaultSubCommand;
    private final List<GpsSubCommand> subCommands;
    private final Supplier<List<GPSGoal>> goalNamesSupplier;
    private final GoalVisibilityService goalVisibilityService;

    public GpsCommand(@NotNull ConfigManager configManager,
                      @NotNull MessageService messageService,
                      @NotNull PermissionService permissionService,
                      @NotNull GpsSubCommand defaultSubCommand,
                      @NotNull List<GpsSubCommand> subCommands,
                      @NotNull Supplier<List<GPSGoal>> goalNamesSupplier,
                      @NotNull GoalVisibilityService goalVisibilityService) {

        this.configManager = configManager;
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.defaultSubCommand = defaultSubCommand;
        this.subCommands = List.copyOf(subCommands);
        this.goalNamesSupplier = goalNamesSupplier;
        this.goalVisibilityService = goalVisibilityService;

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String @NotNull [] args) {

        if (!command.getName().equalsIgnoreCase(COMMAND_NAME)) return false;

        CommandContext context = CommandContext.of(sender);

        if (args.length == 0) {
            return executeSubCommand(defaultSubCommand, context, new String[0]);
        }

        GpsSubCommand subCommand = findSubCommand(args[0]);

        // Первый аргумент не является подкомандой - считаем его названием метки
        if (subCommand == null) {
            return executeSubCommand(defaultSubCommand, context, args);
        }

        return executeSubCommand(subCommand, context, Arrays.copyOfRange(args, 1, args.length));

    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      String @NotNull [] args) {

        if (!command.getName().equalsIgnoreCase(COMMAND_NAME)) return List.of();

        CommandContext context = CommandContext.of(sender);

        if (args.length == 1) {

            // Сначала метки, затем подкоманды - списки не перемешиваются
            List<String> completions = new ArrayList<>(visibleGoalNames(context));
            completions.addAll(availableSubCommands(context));

            return filter(completions, args[0]);

        }

        GpsSubCommand subCommand = findSubCommand(args[0]);
        if (subCommand == null || !hasAccess(context, subCommand)) return List.of();

        return filter(subCommand.complete(context, Arrays.copyOfRange(args, 1, args.length)), args[args.length - 1]);

    }

    private boolean executeSubCommand(@NotNull GpsSubCommand subCommand,
                                      @NotNull CommandContext context,
                                      String @NotNull [] args) {

        if (subCommand.isPlayerOnly() && !context.isPlayer()) {

            messageService.send(context.sender(), configManager.getOnlyPlayersMessage(), Placeholders.create());
            return true;

        }

        if (!hasAccess(context, subCommand)) {

            messageService.send(context.sender(), configManager.getNoPermissionMessage(), Placeholders.create());
            return true;

        }

        return subCommand.execute(context, args);

    }

    private boolean hasAccess(@NotNull CommandContext context, @NotNull GpsSubCommand subCommand) {
        return permissionService.hasPermission(context.sender(), subCommand.getPermission());
    }

    private @Nullable GpsSubCommand findSubCommand(@NotNull String argument) {

        return subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(argument))
                .findFirst()
                .orElse(null);

    }

    private @NotNull List<String> availableSubCommands(@NotNull CommandContext context) {

        return subCommands.stream()
                .filter(subCommand -> !subCommand.isHidden())
                .filter(subCommand -> hasAccess(context, subCommand))
                .filter(subCommand -> !subCommand.isPlayerOnly() || context.isPlayer())
                .map(GpsSubCommand::getName)
                .toList();

    }

    private @NotNull List<String> visibleGoalNames(@NotNull CommandContext context) {

        if (!permissionService.hasPermission(context.sender(), configManager.getPermissionUse())) {
            return List.of();
        }
        return goalVisibilityService.filterVisible(context.sender(), goalNamesSupplier.get())
                .stream()
                .map(GPSGoal::name)
                .toList();

    }

    private @NotNull List<String> filter(@NotNull List<String> values, @NotNull String argument) {

        String prefix = argument.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();

    }
}
