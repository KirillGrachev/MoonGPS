package org.ney.moongps.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.event.GoalNavigateEvent;
import org.ney.moongps.event.GoalReachedEvent;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.model.NavigationSession;
import org.ney.moongps.registry.GoalRegistry;
import org.ney.moongps.util.Placeholders;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис навигации: управляет сессиями игроков,
 * проверяет права и миры, запускает и останавливает задачи.
 * Все публичные методы вызываются из главного потока.
 */
public class NavigationService {

    private final ConfigManager configManager;
    private final GoalRegistry goalRegistry;
    private final NavigationTaskService navigationTaskService;
    private final MessageService messageService;
    private final PermissionService permissionService;
    private final GoalVisibilityService goalVisibilityService;
    private final BossBarService bossBarService;
    private final GoalNotifier goalNotifier;

    private final Map<UUID, NavigationSession> activeSessions = new ConcurrentHashMap<>();

    public NavigationService(@NotNull ConfigManager configManager,
                             @NotNull GoalRegistry goalRegistry,
                             @NotNull NavigationTaskService navigationTaskService,
                             @NotNull MessageService messageService,
                             @NotNull PermissionService permissionService,
                             @NotNull GoalVisibilityService goalVisibilityService,
                             @NotNull BossBarService bossBarService,
                             @NotNull GoalNotifier goalNotifier) {

        this.configManager = configManager;
        this.goalRegistry = goalRegistry;
        this.navigationTaskService = navigationTaskService;
        this.messageService = messageService;
        this.permissionService = permissionService;
        this.goalVisibilityService = goalVisibilityService;
        this.bossBarService = bossBarService;
        this.goalNotifier = goalNotifier;

    }

    /**
     * Включает или выключает навигатор до метки.
     * Повторный вызов с той же меткой выключает навигатор.
     *
     * @param player   игрок
     * @param goalName название метки
     * @param notify   отправлять ли игроку сообщения
     * @return true если навигатор включён
     */
    public boolean toggleGoal(@NotNull Player player, @Nullable String goalName, boolean notify) {

        if (!configManager.isNavigatorEnabled()) {

            sendIfNotify(player, notify, configManager.getNavigationDisabledMessage(), Placeholders.create());
            return false;

        }

        if (!hasPermission(player, configManager.getPermissionUse())) {

            sendIfNotify(player, notify, configManager.getNoPermissionMessage(), Placeholders.create());
            return false;

        }

        if (!isPlayerWorldAllowed(player)) {

            sendIfNotify(player, notify, configManager.getInvalidWorldMessage(),
                    Placeholders.create().add("world", player.getWorld().getName())
            );
            return false;

        }

        GPSGoal goal = goalRegistry.getGoal(goalName);

        if (goal == null || !goalVisibilityService.isVisible(player, goal)) {

            sendIfNotify(player, notify, configManager.getMarkNotFoundMessage(),
                    Placeholders.create().add("gps", Objects.toString(goalName, ""))
            );
            return false;

        }

        NavigationSession currentSession = activeSessions.get(player.getUniqueId());

        if (currentSession != null) {

            if (isSameGoal(currentSession, goal.name())) {

                stopSession(player, currentSession);
                sendIfNotify(player, notify, configManager.getDisabledMessage(), goalPlaceholders(goal));

                return false;

            }

            sendIfNotify(player, notify, configManager.getAlreadyHasGoalMessage(),
                    Placeholders.create()
                            .add("gps", goal.name())
                            .add("current", currentSession.getGoal().name())
            );

            return false;

        }

        if (!goal.isWorldLoaded()) {

            sendIfNotify(player, notify, configManager.getMarkWorldNotLoadedMessage(), goalPlaceholders(goal));
            return false;

        }

        if (!hasGoalPermission(player, goal)) {

            sendIfNotify(player, notify, configManager.getNoPermissionMessage(), goalPlaceholders(goal));
            return false;

        }

        Location goalLocation = goal.toLocation();

        if (goalLocation != null
                && player.getLocation().distance(goalLocation) <= configManager.getReachDistance()) {

            sendIfNotify(player, notify, configManager.getAlreadyAtMarkMessage(), goalPlaceholders(goal));
            return false;
        }

        GoalNavigateEvent navigateEvent = new GoalNavigateEvent(player, goal);
        Bukkit.getPluginManager().callEvent(navigateEvent);

        if (navigateEvent.isCancelled()) return false;

        startSession(player, goal);
        sendIfNotify(player, notify, configManager.getEnabledMessage(), goalPlaceholders(goal));

        return true;

    }

    /**
     * Останавливает навигатор игрока.
     *
     * @param player игрок
     * @param notify отправлять ли игроку сообщение о выключении
     * @return true если навигатор был включён
     */
    public boolean stopNavigation(@NotNull Player player, boolean notify) {

        NavigationSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return false;

        bossBarService.remove(player.getUniqueId());
        session.stop();

        if (notify) {
            messageService.send(player, configManager.getDisabledMessage(), goalPlaceholders(session.getGoal()));
        }

        return true;

    }

    /**
     * Останавливает навигатор у всех игроков, идущих к указанной метке.
     * Используется при удалении или перезаписи метки.
     *
     * @param goalName название метки
     */
    public void stopGoalForEveryone(@NotNull String goalName) {

        activeSessions.values().stream()
                .filter(session -> isSameGoal(session, goalName))
                .toList()
                .forEach(this::stopSessionSilently);

    }

    /**
     * Обновляет метку у всех игроков, которые идут к ней.
     *
     * @param goalName название метки
     */
    public void refreshGoalForEveryone(@NotNull String goalName) {

        GPSGoal goal = goalRegistry.getGoal(goalName);
        if (goal == null) return;

        activeSessions.values().stream()
                .filter(session -> isSameGoal(session, goalName))
                .forEach(session -> session.setGoal(goal));

    }

    /**
     * Останавливает навигацию всех игроков (перезагрузка конфигурации).
     * Активные метки сбрасываются: продолжать путь по старым сессиям нельзя.
     *
     * @param notify сообщать ли игрокам о выключении навигатора
     */
    public void stopAll(boolean notify) {

        List<NavigationSession> sessions = List.copyOf(activeSessions.values());

        sessions.forEach(session -> {

            Player player = session.getPlayer();

            stopSessionSilently(session);

            if (notify && player != null) {

                messageService.send(player, configManager.getDisabledMessage(),
                        goalPlaceholders(session.getGoal())
                );
            }

        });
    }

    /**
     * Останавливает все сессии (при выключении плагина).
     */
    public void cancelAll() {

        stopAll(false);
        bossBarService.removeAll();

    }

    /**
     * Возвращает метку, до которой идёт игрок.
     *
     * @param player игрок
     * @return метка или null, если навигатор выключен
     */
    public @Nullable GPSGoal getActiveGoal(@NotNull Player player) {

        NavigationSession session = activeSessions.get(player.getUniqueId());
        return session == null ? null : session.getGoal();

    }

    public int getActiveSessionsCount() {
        return activeSessions.size();
    }

    /** Внутренняя логика сессий */

    private void startSession(@NotNull Player player, @NotNull GPSGoal goal) {

        NavigationSession session = new NavigationSession(player.getUniqueId(), goal);
        activeSessions.put(player.getUniqueId(), session);

        navigationTaskService.start(session, this::handleGoalReached);

    }

    private void handleGoalReached(@NotNull Player player, @NotNull GPSGoal goal) {

        if (!player.isOnline()) return;

        activeSessions.remove(player.getUniqueId());
        bossBarService.remove(player.getUniqueId());

        GoalReachedEvent reachedEvent = new GoalReachedEvent(player, goal);
        Bukkit.getPluginManager().callEvent(reachedEvent);

        if (reachedEvent.isCancelled()) return;

        goalNotifier.notifyGoalReached(player, goal);

    }

    private void stopSession(@NotNull Player player, @NotNull NavigationSession session) {

        activeSessions.remove(player.getUniqueId());
        bossBarService.remove(player.getUniqueId());
        session.stop();

    }

    private void stopSessionSilently(@NotNull NavigationSession session) {

        activeSessions.remove(session.getPlayerUUID());
        bossBarService.remove(session.getPlayerUUID());
        session.stop();

    }

    private boolean isSameGoal(@NotNull NavigationSession session, @NotNull String goalName) {
        return session.getGoal().name().equalsIgnoreCase(goalName);
    }

    /** Проверки прав и миров */

    private boolean hasPermission(@NotNull Player player, @Nullable String permission) {
        return permissionService.hasPermission(player, permission);
    }

    private boolean hasGoalPermission(@NotNull Player player, @NotNull GPSGoal goal) {
        return hasPermission(player, goal.permission());
    }

    private boolean isPlayerWorldAllowed(@NotNull Player player) {

        if (!configManager.areWorldsRestricted()) return true;
        return configManager.getAllowedWorlds().contains(player.getWorld().getName());

    }

    private void sendIfNotify(@NotNull Player player,
                              boolean notify,
                              @NotNull Messages messages,
                              @NotNull Placeholders placeholders) {

        if (!notify) return;
        messageService.send(player, messages, placeholders);

    }

    private @NotNull Placeholders goalPlaceholders(@NotNull GPSGoal goal) {

        return Placeholders.create()
                .add("gps", goal.name())
                .add("world", goal.world());

    }

}
