package org.ney.moongps.service;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.Direction;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.model.NavigationSession;
import org.ney.moongps.registry.GoalRegistry;
import org.ney.moongps.util.Placeholders;

import java.util.function.BiConsumer;

/**
 * Сервис задач навигации.
 * Расчёт направления выполняется асинхронно, а вывод игроку -
 * всегда в главном потоке (титулы и action bar не потокобезопасны).
 */
public class NavigationTaskService {

    private final MoonGPS plugin;
    private final ConfigManager configManager;
    private final GoalRegistry goalRegistry;
    private final DirectionService directionService;
    private final GoalNotifier goalNotifier;
    private final MessageService messageService;

    public NavigationTaskService(@NotNull MoonGPS plugin,
                                 @NotNull ConfigManager configManager,
                                 @NotNull GoalRegistry goalRegistry,
                                 @NotNull DirectionService directionService,
                                 @NotNull GoalNotifier goalNotifier,
                                 @NotNull MessageService messageService) {

        this.plugin = plugin;
        this.configManager = configManager;
        this.goalRegistry = goalRegistry;
        this.directionService = directionService;
        this.goalNotifier = goalNotifier;
        this.messageService = messageService;

    }

    /**
     * Запускает периодическую задачу навигации для сессии.
     *
     * @param session  сессия навигации
     * @param onReach  действие при достижении метки (вызывается в главном потоке)
     */
    public void start(@NotNull NavigationSession session,
                      @NotNull BiConsumer<Player, GPSGoal> onReach) {

        long interval = configManager.getNavigationInterval();

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin, () -> tick(session, onReach), 0L, interval
        );

        session.attachTask(task);

    }

    /**
     * Один цикл обновления навигации (асинхронный поток).
     *
     * @param session сессия навигации
     * @param onReach действие при достижении метки
     */
    private void tick(@NotNull NavigationSession session,
                      @NotNull BiConsumer<Player, GPSGoal> onReach) {

        if (session.isStopped()) return;

        Player player = session.getPlayer();

        if (player == null || !player.isOnline()) {

            session.stop();
            return;

        }

        GPSGoal goal = session.getGoal();

        // Метка могла быть удалена или перезагружена во время навигации
        if (goalRegistry.getGoal(goal.name()) == null) {

            session.stop();
            return;

        }

        Location goalLocation = goal.toLocation();

        if (goalLocation == null) {

            session.stop();
            notifyMarkWorldNotLoaded(player, goal);

            return;

        }

        Location playerLocation = player.getLocation();

        if (!isSameWorld(playerLocation, goalLocation)) {

            if (!configManager.shouldStopOnWorldChange()) {
                return; // Ждём, пока игрок вернётся в мир метки
            }

            session.stop();
            notifyWorldLeft(player);

            return;

        }

        double distance = directionService.calculateDistance(playerLocation, goalLocation);

        if (distance <= configManager.getReachDistance()) {

            session.stop();
            runSync(() -> onReach.accept(player, goal));

            return;

        }

        Direction direction = directionService.calculateDirection(playerLocation, goalLocation);

        runSync(() -> {

            if (session.isStopped() || !player.isOnline()) return;
            goalNotifier.notifyNavigation(player, goal, direction, distance);

        });

    }

    private void notifyMarkWorldNotLoaded(@NotNull Player player, @NotNull GPSGoal goal) {

        runSync(() -> {

            if (!player.isOnline()) return;
            messageService.send(player,
                    configManager.getMarkWorldNotLoadedMessage(),
                    Placeholders.create()
                            .add("gps", goal.name())
                            .add("world", goal.world())
            );

        });

    }

    private boolean isSameWorld(@NotNull Location playerLocation, @NotNull Location goalLocation) {

        return playerLocation.getWorld() != null
                && playerLocation.getWorld().equals(goalLocation.getWorld());

    }

    private void notifyWorldLeft(@NotNull Player player) {

        runSync(() -> {

            if (!player.isOnline()) return;
            messageService.send(player,
                    configManager.getStoppedWorldChangedMessage(),
                    Placeholders.create()
            );

        });

    }

    private void runSync(@NotNull Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
}
