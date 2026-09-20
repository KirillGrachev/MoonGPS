package org.ney.moongps.service;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.event.GoalNavigateEvent;
import org.ney.moongps.event.GoalReachedEvent;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.model.NavigationSession;
import org.ney.moongps.registry.GoalRegistry;
import org.ney.moongps.support.BukkitSupport;
import org.ney.moongps.util.Placeholders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigationServiceTest {

    private static final GPSGoal GOAL = new GPSGoal("shop", 10.0D, 70.0D, 10.0D, "world", null);

    private ConfigManager configManager;
    private GoalRegistry goalRegistry;
    private NavigationTaskService navigationTaskService;
    private MessageService messageService;
    private PermissionService permissionService;
    private NavigationService navigationService;
    private GoalVisibilityService goalVisibilityService;
    private BossBarService bossBarService;
    private GoalNotifier goalNotifier;
    private PluginManager pluginManager;
    private Player player;
    private World world;

    private Messages navigationDisabledMessage;
    private Messages noPermissionMessage;
    private Messages invalidWorldMessage;
    private Messages markNotFoundMessage;
    private Messages disabledMessage;
    private Messages alreadyHasGoalMessage;
    private Messages markWorldNotLoadedMessage;
    private Messages alreadyAtMarkMessage;
    private Messages enabledMessage;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        goalRegistry = Mockito.mock(GoalRegistry.class);
        navigationTaskService = Mockito.mock(NavigationTaskService.class);
        messageService = Mockito.mock(MessageService.class);
        permissionService = Mockito.mock(PermissionService.class);

        goalVisibilityService = Mockito.mock(GoalVisibilityService.class);
        bossBarService = Mockito.mock(BossBarService.class);
        goalNotifier = Mockito.mock(GoalNotifier.class);

        Mockito.when(goalVisibilityService.isVisible(Mockito.any(), Mockito.any())).thenReturn(true);

        navigationService = new NavigationService(
                configManager, goalRegistry, navigationTaskService, messageService,
                permissionService, goalVisibilityService, bossBarService, goalNotifier
        );

        pluginManager = BukkitSupport.newPluginManager();

        player = Mockito.mock(Player.class);
        world = BukkitSupport.world("world");

        Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        Mockito.when(player.getWorld()).thenReturn(world);
        Mockito.when(player.isOnline()).thenReturn(true);
        Mockito.when(player.getLocation()).thenReturn(new Location(world, 0.0D, 70.0D, 0.0D));

        BukkitSupport.onlinePlayer(player);

        navigationDisabledMessage = new Messages(List.of("navigator disabled"), true);
        noPermissionMessage = new Messages(List.of("no permission"), true);
        invalidWorldMessage = new Messages(List.of("invalid world"), true);
        markNotFoundMessage = new Messages(List.of("not found"), true);
        disabledMessage = new Messages(List.of("disabled"), true);
        alreadyHasGoalMessage = new Messages(List.of("already"), true);
        markWorldNotLoadedMessage = new Messages(List.of("world not loaded"), true);
        enabledMessage = new Messages(List.of("enabled"), true);

        Mockito.when(configManager.getNavigationDisabledMessage()).thenReturn(navigationDisabledMessage);
        Mockito.when(configManager.getNoPermissionMessage()).thenReturn(noPermissionMessage);
        Mockito.when(configManager.getInvalidWorldMessage()).thenReturn(invalidWorldMessage);
        Mockito.when(configManager.getMarkNotFoundMessage()).thenReturn(markNotFoundMessage);
        Mockito.when(configManager.getDisabledMessage()).thenReturn(disabledMessage);
        Mockito.when(configManager.getAlreadyHasGoalMessage()).thenReturn(alreadyHasGoalMessage);
        Mockito.when(configManager.getMarkWorldNotLoadedMessage()).thenReturn(markWorldNotLoadedMessage);
        alreadyAtMarkMessage = new Messages(List.of("already at mark"), true);

        Mockito.when(configManager.getAlreadyAtMarkMessage()).thenReturn(alreadyAtMarkMessage);
        Mockito.when(configManager.getEnabledMessage()).thenReturn(enabledMessage);

        Mockito.when(configManager.isNavigatorEnabled()).thenReturn(true);
        Mockito.when(configManager.getPermissionUse()).thenReturn("moongps.use");
        Mockito.when(configManager.areWorldsRestricted()).thenReturn(true);
        Mockito.when(configManager.getAllowedWorlds()).thenReturn(List.of("world"));
        Mockito.when(permissionService.hasPermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(goalRegistry.getGoal("shop")).thenReturn(GOAL);

    }

    @Test
    @DisplayName("Выключенный навигатор не запускается")
    void navigatorDisabled() {

        Mockito.when(configManager.isNavigatorEnabled()).thenReturn(false);

        assertFalse(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(navigationDisabledMessage), Mockito.any(Placeholders.class));
        Mockito.verify(navigationTaskService, Mockito.never()).start(Mockito.any(), Mockito.any());

    }

    @Test
    @DisplayName("Без права использования навигатор не запускается")
    void noUsePermission() {

        Mockito.when(permissionService.hasPermission(player, "moongps.use")).thenReturn(false);

        assertFalse(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(noPermissionMessage), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("В запрещённом мире навигатор не запускается")
    void wrongWorld() {

        World nether = BukkitSupport.world("nether");

        Mockito.when(player.getWorld()).thenReturn(nether);

        assertFalse(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(invalidWorldMessage), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Навигатор не включается до метки, на которой игрок стоит")
    void toggleAtMark() {

        Mockito.when(player.getLocation()).thenReturn(new Location(world, 10.0D, 70.0D, 10.0D));

        assertFalse(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(alreadyAtMarkMessage), Mockito.any(Placeholders.class));
        Mockito.verify(navigationTaskService, Mockito.never()).start(Mockito.any(), Mockito.any());

    }

    @Test
    @DisplayName("Скрытая метка чужого мира считается ненайденной")
    void hiddenGoalNotFound() {

        Mockito.when(goalVisibilityService.isVisible(Mockito.eq(player), Mockito.eq(GOAL))).thenReturn(false);

        assertFalse(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(markNotFoundMessage), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Несуществующая метка даёт сообщение об ошибке")
    void goalNotFound() {

        assertFalse(navigationService.toggleGoal(player, "unknown", true));
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(markNotFoundMessage), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Повторный вызов той же метки выключает навигатор")
    void toggleOff() {

        assertTrue(navigationService.toggleGoal(player, "shop", true));
        assertFalse(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(disabledMessage), Mockito.any(Placeholders.class));
        Mockito.verify(navigationTaskService, Mockito.times(1)).start(Mockito.any(), Mockito.any());

    }

    @Test
    @DisplayName("Вторая метка при активной не включается")
    void alreadyHasGoal() {

        GPSGoal second = new GPSGoal("bank", 1.0D, 2.0D, 3.0D, "world", null);

        Mockito.when(goalRegistry.getGoal("bank")).thenReturn(second);

        assertTrue(navigationService.toggleGoal(player, "shop", false));
        assertFalse(navigationService.toggleGoal(player, "bank", true));

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(alreadyHasGoalMessage), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Метка из незагруженного мира не включается")
    void goalWorldNotLoaded() {

        BukkitSupport.clearWorlds();

        assertFalse(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(markWorldNotLoadedMessage), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Личное право метки проверяется отдельно")
    void goalPermissionRequired() {

        GPSGoal restricted = new GPSGoal("bank", 1.0D, 2.0D, 3.0D, "world", "moongps.mark.bank");

        Mockito.when(goalRegistry.getGoal("bank")).thenReturn(restricted);
        Mockito.when(permissionService.hasPermission(player, "moongps.mark.bank")).thenReturn(false);

        assertFalse(navigationService.toggleGoal(player, "bank", true));

        Mockito.verify(permissionService).hasPermission(player, "moongps.mark.bank");

    }

    @Test
    @DisplayName("Отмена события GoalNavigateEvent останавливает включение")
    void navigateEventCancelled() {

        Mockito.doAnswer(invocation -> {

            GoalNavigateEvent event = invocation.getArgument(0);
            event.setCancelled(true);

            return null;

        }).when(pluginManager).callEvent(Mockito.any(GoalNavigateEvent.class));

        assertFalse(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(navigationTaskService, Mockito.never()).start(Mockito.any(), Mockito.any());

    }

    @Test
    @DisplayName("Успешное включение запускает задачу и сообщает игроку")
    void toggleOn() {

        assertTrue(navigationService.toggleGoal(player, "shop", true));

        Mockito.verify(navigationTaskService).start(Mockito.any(NavigationSession.class), Mockito.any());
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(enabledMessage), Mockito.any(Placeholders.class));
        assertEquals(GOAL, navigationService.getActiveGoal(player));
        assertEquals(1, navigationService.getActiveSessionsCount());

    }

    @Test
    @DisplayName("Остановка снимает сессию и задачу")
    void stopNavigation() {

        navigationService.toggleGoal(player, "shop", false);

        assertTrue(navigationService.stopNavigation(player, false));
        assertFalse(navigationService.stopNavigation(player, false));

        Mockito.verify(bossBarService).remove(player.getUniqueId());

        assertNull(navigationService.getActiveGoal(player));
        assertEquals(0, navigationService.getActiveSessionsCount());

    }

    @Test
    @DisplayName("Удаление метки останавливает всех, кто к ней идёт")
    void stopGoalForEveryone() {

        navigationService.toggleGoal(player, "shop", false);

        navigationService.stopGoalForEveryone("shop");

        assertEquals(0, navigationService.getActiveSessionsCount());

    }

    @Test
    @DisplayName("stopAll сбрасывает активные метки и сообщает игрокам")
    void stopAllNotifies() {

        navigationService.toggleGoal(player, "shop", false);

        navigationService.stopAll(true);

        assertEquals(0, navigationService.getActiveSessionsCount());
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(disabledMessage), Mockito.any(Placeholders.class));
        Mockito.verify(bossBarService).remove(player.getUniqueId());

    }

    @Test
    @DisplayName("stopAll без оповещения работает тихо")
    void stopAllSilent() {

        navigationService.toggleGoal(player, "shop", false);

        navigationService.stopAll(false);

        assertEquals(0, navigationService.getActiveSessionsCount());
        Mockito.verify(messageService, Mockito.never()).send(Mockito.eq(player), Mockito.eq(disabledMessage), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("cancelAll снимает все сессии")
    void cancelAll() {

        navigationService.toggleGoal(player, "shop", false);

        navigationService.cancelAll();

        assertEquals(0, navigationService.getActiveSessionsCount());

    }

    @Test
    @DisplayName("Событие включения вызывается в Bukkit")
    void navigateEventFired() {

        navigationService.toggleGoal(player, "shop", false);
        Mockito.verify(pluginManager).callEvent(Mockito.any(GoalNavigateEvent.class));

    }

    @Test
    @DisplayName("Сессия хранит игрока по UUID")
    void sessionUsesPlayerUuid() {

        navigationService.toggleGoal(player, "shop", false);

        NavigationSession session = captureSession();

        assertEquals(player.getUniqueId(), session.getPlayerUUID());

    }

    @Test
    @DisplayName("Достижение метки из задачи снимает сессию и оповещает игрока")
    void goalReachedFromTask() {

        navigationService.toggleGoal(player, "shop", false);

        captureOnReach().accept(player, GOAL);

        Mockito.verify(pluginManager).callEvent(Mockito.any(GoalReachedEvent.class));
        Mockito.verify(goalNotifier).notifyGoalReached(player, GOAL);
        assertEquals(0, navigationService.getActiveSessionsCount());

    }

    @Test
    @DisplayName("Отмена события достижения подавляет оповещение")
    void goalReachedCancelled() {

        Mockito.doAnswer(invocation -> {

            GoalReachedEvent event = invocation.getArgument(0);
            event.setCancelled(true);

            return null;

        }).when(pluginManager).callEvent(Mockito.any(GoalReachedEvent.class));

        navigationService.toggleGoal(player, "shop", false);

        captureOnReach().accept(player, GOAL);

        Mockito.verify(goalNotifier, Mockito.never()).notifyGoalReached(Mockito.any(), Mockito.any());

    }

    @SuppressWarnings("unchecked")
    private java.util.function.BiConsumer<Player, GPSGoal> captureOnReach() {

        org.mockito.ArgumentCaptor<java.util.function.BiConsumer<Player, GPSGoal>> captor =
                org.mockito.ArgumentCaptor.forClass(java.util.function.BiConsumer.class);

        Mockito.verify(navigationTaskService).start(Mockito.any(), captor.capture());

        return captor.getValue();

    }

    private NavigationSession captureSession() {

        org.mockito.ArgumentCaptor<NavigationSession> captor = org.mockito.ArgumentCaptor.forClass(NavigationSession.class);

        Mockito.verify(navigationTaskService).start(captor.capture(), Mockito.any());

        return captor.getValue();

    }
}
