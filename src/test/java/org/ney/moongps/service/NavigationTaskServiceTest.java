package org.ney.moongps.service;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.config.type.DirectionMode;
import org.ney.moongps.config.type.DirectionSettings;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.model.NavigationSession;
import org.ney.moongps.registry.GoalRegistry;
import org.ney.moongps.support.BukkitSupport;
import org.ney.moongps.util.Placeholders;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigationTaskServiceTest {

    private static final GPSGoal GOAL = new GPSGoal("shop", 10.0D, 70.0D, 10.0D, "world", null);

    private MoonGPS plugin;
    private ConfigManager configManager;
    private GoalRegistry goalRegistry;
    private GoalNotifier goalNotifier;
    private MessageService messageService;
    private NavigationTaskService navigationTaskService;
    private BukkitScheduler scheduler;
    private Player player;
    private World world;
    private NavigationSession session;

    @BeforeEach
    void setUp() {

        plugin = Mockito.mock(MoonGPS.class);
        configManager = Mockito.mock(ConfigManager.class);
        goalRegistry = Mockito.mock(GoalRegistry.class);
        goalNotifier = Mockito.mock(GoalNotifier.class);
        messageService = Mockito.mock(MessageService.class);

        scheduler = Mockito.mock(BukkitScheduler.class);

        Mockito.when(BukkitSupport.server().getScheduler()).thenReturn(scheduler);
        Mockito.when(plugin.getServer()).thenReturn(BukkitSupport.server());

        navigationTaskService = new NavigationTaskService(
                plugin, configManager, goalRegistry,
                new DirectionService(configManager), goalNotifier, messageService
        );

        world = BukkitSupport.world("world");
        player = Mockito.mock(Player.class);

        Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        Mockito.when(player.isOnline()).thenReturn(true);
        Mockito.when(player.getLocation()).thenReturn(new Location(world, 0.0D, 70.0D, 0.0D));
        BukkitSupport.onlinePlayer(player);

        Mockito.when(configManager.getNavigationInterval()).thenReturn(8L);
        Mockito.when(configManager.getReachDistance()).thenReturn(2.0D);
        Mockito.when(configManager.shouldStopOnWorldChange()).thenReturn(true);
        Mockito.when(configManager.getMarkWorldNotLoadedMessage()).thenReturn(new Messages(List.of("not loaded"), true));
        Mockito.when(configManager.getStoppedWorldChangedMessage()).thenReturn(new Messages(List.of("stopped"), true));
        Mockito.when(configManager.getDirectionSettings()).thenReturn(new DirectionSettings(
                DirectionMode.RELATIVE, 0.85D, -0.85D, "{arrow} {name}", Map.of(), Map.of()
        ));
        Mockito.when(goalRegistry.getGoal("shop")).thenReturn(GOAL);

        session = new NavigationSession(player.getUniqueId(), GOAL);

    }

    @Test
    @DisplayName("Задача планируется с периодом из конфига")
    void taskScheduledWithInterval() {

        Mockito.when(scheduler.runTaskTimerAsynchronously(Mockito.eq(plugin), Mockito.any(Runnable.class), Mockito.eq(0L), Mockito.eq(8L)))
                .thenReturn(Mockito.mock(BukkitTask.class));

        navigationTaskService.start(session, (reachedPlayer, goal) -> {

        });

        Mockito.verify(scheduler).runTaskTimerAsynchronously(
                Mockito.eq(plugin), Mockito.any(Runnable.class), Mockito.eq(0L), Mockito.eq(8L)
        );

    }

    @Test
    @DisplayName("Игрок не в сети - сессия останавливается")
    void playerOffline() {

        Mockito.when(player.isOnline()).thenReturn(false);

        tick((reachedPlayer, goal) -> {

        });

        assertTrue(session.isStopped());
        Mockito.verifyNoInteractions(goalNotifier);

    }

    @Test
    @DisplayName("Удалённая метка останавливает сессию без сообщений")
    void goalRemoved() {

        Mockito.when(goalRegistry.getGoal("shop")).thenReturn(null);

        tick((reachedPlayer, goal) -> {

        });

        assertTrue(session.isStopped());
        Mockito.verifyNoInteractions(messageService);

    }

    @Test
    @DisplayName("Незагруженный мир метки останавливает сессию с сообщением")
    void goalWorldNotLoaded() {

        BukkitSupport.clearWorlds();

        tick((reachedPlayer, goal) -> {

        });

        assertTrue(session.isStopped());
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Выход из мира метки выключает навигатор")
    void playerLeftWorld() {

        World nether = BukkitSupport.world("nether");
        Messages stoppedMessage = new Messages(List.of("stopped"), true);

        Mockito.when(configManager.getStoppedWorldChangedMessage()).thenReturn(stoppedMessage);
        Mockito.when(player.getLocation()).thenReturn(new Location(nether, 0.0D, 70.0D, 0.0D));

        tick((reachedPlayer, goal) -> {

        });

        assertTrue(session.isStopped());
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("С выключенным stop_on_world_change навигатор ждёт возвращения")
    void playerLeftWorldWithoutStop() {

        World nether = BukkitSupport.world("nether");

        Mockito.when(configManager.shouldStopOnWorldChange()).thenReturn(false);
        Mockito.when(player.getLocation()).thenReturn(new Location(nether, 0.0D, 70.0D, 0.0D));

        tick((reachedPlayer, goal) -> {

        });

        assertFalse(session.isStopped());
        Mockito.verifyNoInteractions(goalNotifier);

    }

    @Test
    @DisplayName("Достижение метки вызывает обработчик и останавливает сессию")
    void goalReached() {

        Mockito.when(player.getLocation()).thenReturn(new Location(world, 10.0D, 70.0D, 11.0D));

        boolean[] reached = new boolean[1];

        tick((reachedPlayer, goal) -> reached[0] = true);

        assertTrue(session.isStopped());
        assertTrue(reached[0]);
        Mockito.verifyNoInteractions(goalNotifier);

    }

    @Test
    @DisplayName("Обычный тик выводит направление через notifier")
    void navigationRendered() {

        tick((reachedPlayer, goal) -> {

        });

        assertFalse(session.isStopped());
        Mockito.verify(goalNotifier).notifyNavigation(Mockito.eq(player), Mockito.eq(GOAL), Mockito.any(), Mockito.eq(Math.sqrt(200.0D)));

    }

    private void tick(BiConsumer<Player, GPSGoal> onReach) {

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        Mockito.when(scheduler.runTaskTimerAsynchronously(Mockito.eq(plugin), captor.capture(), Mockito.eq(0L), Mockito.eq(8L)))
                .thenReturn(Mockito.mock(BukkitTask.class));

        // Вывод игроку в тестах выполняется сразу в том же потоке
        Mockito.when(scheduler.runTask(Mockito.eq(plugin), Mockito.any(Runnable.class)))
                .thenAnswer(invocation -> {

                    Runnable runnable = invocation.getArgument(1);
                    runnable.run();

                    return null;

                });

        navigationTaskService.start(session, onReach);
        captor.getValue().run();

    }
}
