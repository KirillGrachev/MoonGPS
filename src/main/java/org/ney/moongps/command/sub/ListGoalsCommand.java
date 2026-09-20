package org.ney.moongps.command.sub;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.command.CommandContext;
import org.ney.moongps.command.GpsSubCommand;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.service.GoalVisibilityService;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.util.Placeholders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Подкоманда /gps list [страница].
 * Показывает список меток с расстоянием до них, разбитый на страницы.
 */
public class ListGoalsCommand implements GpsSubCommand {

    private final ConfigManager configManager;
    private final MessageService messageService;
    private final Supplier<List<GPSGoal>> goalsSupplier;
    private final GoalVisibilityService goalVisibilityService;

    public ListGoalsCommand(@NotNull ConfigManager configManager,
                            @NotNull MessageService messageService,
                            @NotNull Supplier<List<GPSGoal>> goalsSupplier,
                            @NotNull GoalVisibilityService goalVisibilityService) {

        this.configManager = configManager;
        this.messageService = messageService;
        this.goalsSupplier = goalsSupplier;
        this.goalVisibilityService = goalVisibilityService;

    }

    @Override
    public @NotNull String getName() {
        return "list";
    }

    @Override
    public String getPermission() {
        return configManager.getPermissionList();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        List<GPSGoal> goalList = sortedGoals(context);

        if (goalList.isEmpty()) {

            messageService.send(context.sender(), configManager.getListEmptyMessage(), Placeholders.create());
            return true;

        }

        int maxPage = (int) Math.ceil((double) goalList.size() / entriesPerPage());
        int page = parsePage(args, maxPage);

        List<String> lines = buildPage(context, goalList, page, maxPage);

        lines.forEach(context.sender()::sendMessage);

        return true;

    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandContext context, String @NotNull [] args) {

        if (args.length != 1) return List.of();

        int maxPage = (int) Math.ceil((double) sortedGoals(context).size() / entriesPerPage());

        List<String> pages = new ArrayList<>();
        for (int page = 1; page <= Math.max(1, maxPage); page++) {
            pages.add(String.valueOf(page));
        }

        return pages;

    }

    private @NotNull List<String> buildPage(@NotNull CommandContext context,
                                            @NotNull List<GPSGoal> goalList,
                                            int page,
                                            int maxPage) {

        List<String> lines = new ArrayList<>();

        addAll(lines, configManager.getListHeader().values(), page, maxPage, goalList.size());

        int fromIndex = (page - 1) * entriesPerPage();
        int toIndex = Math.min(fromIndex + entriesPerPage(), goalList.size());

        for (GPSGoal goal : goalList.subList(fromIndex, toIndex)) {
            lines.add(buildEntry(context, goal));
        }

        addAll(lines, configManager.getListFooter().values(), page, maxPage, goalList.size());

        return lines;

    }

    private @NotNull String buildEntry(@NotNull CommandContext context, @NotNull GPSGoal goal) {

        Location playerLocation = context.requirePlayer().getLocation();
        Location goalLocation = goal.toLocation();

        boolean sameWorld = goalLocation != null
                && goalLocation.getWorld() != null
                && goalLocation.getWorld().equals(playerLocation.getWorld());

        Messages format = sameWorld
                ? configManager.getListEntryFormat()
                : configManager.getListOtherWorldEntryFormat();

        if (format.isEmpty()) return "";

        Placeholders placeholders = Placeholders.create()
                .add("gps", goal.name())
                .add("world", goal.world())
                .add("distance", sameWorld
                        ? String.valueOf((int) playerLocation.distance(goalLocation))
                        : "?");

        return placeholders.apply(format.values().get(0));

    }

    private void addAll(@NotNull List<String> lines, @NotNull List<String> values,
                        int page, int maxPage, int total) {

        values.forEach(line -> lines.add(Placeholders.create()
                .add("page", page)
                .add("max_page", maxPage)
                .add("total", total)
                .apply(line))
        );

    }

    private int entriesPerPage() {
        return configManager.getListPerPage();
    }

    private int parsePage(String @NotNull [] args, int maxPage) {

        if (args.length == 0) return 1;
        try {

            int page = Integer.parseInt(args[0]);
            return Math.max(1, Math.min(page, maxPage));

        } catch (NumberFormatException exception) {
            return 1;
        }

    }

    private @NotNull List<GPSGoal> sortedGoals(@NotNull CommandContext context) {

        List<GPSGoal> goalList = new ArrayList<>(
                goalVisibilityService.filterVisible(context.sender(), goalsSupplier.get())
        );

        goalList.sort((first, second) -> first.name().compareToIgnoreCase(second.name()));

        return goalList;

    }
}
