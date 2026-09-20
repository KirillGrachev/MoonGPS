package org.ney.moongps.service;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.config.message.MoonTitle;
import org.ney.moongps.config.type.BossBarProgress;
import org.ney.moongps.config.type.DisplaySettings;
import org.ney.moongps.config.type.ReachSettings;
import org.ney.moongps.model.Direction;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.util.Placeholders;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.util.List;

class GoalNotifierTest {

    private static final GPSGoal GOAL = new GPSGoal("shop", 1.0D, 2.0D, 3.0D, "lobby", null);

    private ConfigManager configManager;
    private MessageService messageService;
    private DirectionService directionService;
    private BossBarService bossBarService;
    private GoalNotifier goalNotifier;
    private Player player;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        messageService = Mockito.mock(MessageService.class);
        bossBarService = Mockito.mock(BossBarService.class);

        directionService = Mockito.mock(DirectionService.class);
        Mockito.when(directionService.format(Mockito.any(Direction.class))).thenReturn("→");

        goalNotifier = new GoalNotifier(configManager, messageService, directionService, bossBarService);

        player = Mockito.mock(Player.class);

        display(true, true, true, true);

    }

    @Test
    @DisplayName("Титул выводится только своим каналом")
    void titleOnly() {

        display(true, false, false, false);

        goalNotifier.notifyNavigation(player, GOAL, direction(), 12.0D);

        Mockito.verify(messageService).sendTitle(Mockito.eq(player), Mockito.any(MoonTitle.class), Mockito.any(Placeholders.class));
        Mockito.verify(messageService, Mockito.never()).sendActionBar(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));
        Mockito.verifyNoInteractions(bossBarService);

    }

    @Test
    @DisplayName("Action bar выводится только своим каналом")
    void actionBarOnly() {

        display(false, true, false, false);

        goalNotifier.notifyNavigation(player, GOAL, direction(), 12.0D);

        Mockito.verify(messageService).sendActionBar(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));
        Mockito.verify(messageService, Mockito.never()).sendTitle(Mockito.eq(player), Mockito.any(MoonTitle.class), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Boss bar обновляется текстом с плейсхолдерами")
    void bossBarOnly() {

        display(false, false, true, false);

        goalNotifier.notifyNavigation(player, GOAL, direction(), 12.0D);

        Mockito.verify(bossBarService).update(Mockito.eq(player), Mockito.eq("→ 12 bl. to shop"), Mockito.eq(12.0D));
        Mockito.verifyNoInteractions(messageService);

    }

    @Test
    @DisplayName("Пустой текст boss bar не создаёт полосу")
    void bossBarWithoutText() {

        Mockito.when(configManager.getDisplaySettings()).thenReturn(new DisplaySettings(
                false, false, true, false,
                MoonTitle.disabled(), Messages.disabled(), Messages.disabled(), Messages.disabled(),
                BarColor.BLUE, BarStyle.SOLID, BossBarProgress.DISTANCE
        ));

        goalNotifier.notifyNavigation(player, GOAL, direction(), 12.0D);

        Mockito.verifyNoInteractions(bossBarService);

    }

    @Test
    @DisplayName("Каналы комбинируются свободно")
    void allChannels() {

        display(true, true, true, true);

        goalNotifier.notifyNavigation(player, GOAL, direction(), 12.0D);

        Mockito.verify(messageService).sendTitle(Mockito.eq(player), Mockito.any(MoonTitle.class), Mockito.any(Placeholders.class));
        Mockito.verify(messageService).sendActionBar(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));
        Mockito.verify(bossBarService).update(Mockito.eq(player), Mockito.anyString(), Mockito.eq(12.0D));

    }

    @Test
    @DisplayName("Достижение метки: титул, action bar, чат и звук")
    void goalReachedFull() {

        ReachSettings.SoundEffect sound = new ReachSettings.SoundEffect(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.5F);

        Mockito.when(configManager.getReachSettings()).thenReturn(new ReachSettings(
                new MoonTitle("&6Navigator", "&fMark reached!", 0, 40, 10),
                new Messages(List.of("reached"), true),
                new Messages(List.of("bar"), true),
                new Messages(List.of("✔ reached {gps}"), true),
                BarColor.GREEN, BarStyle.SOLID, 40L,
                sound
        ));

        Mockito.when(player.getLocation()).thenReturn(new org.bukkit.Location(null, 0.0D, 0.0D, 0.0D));

        goalNotifier.notifyGoalReached(player, GOAL);

        Mockito.verify(messageService).sendTitle(Mockito.eq(player), Mockito.any(MoonTitle.class), Mockito.any(Placeholders.class));
        Mockito.verify(messageService).sendActionBar(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));
        Mockito.verify(player).playSound(Mockito.any(org.bukkit.Location.class), Mockito.eq(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP), Mockito.eq(1.0F), Mockito.eq(1.5F));
        Mockito.verify(bossBarService).flash(Mockito.eq(player), Mockito.eq("✔ reached shop"), Mockito.eq(BarColor.GREEN), Mockito.eq(BarStyle.SOLID), Mockito.eq(40L));

    }

    @Test
    @DisplayName("Выключенный мастер-канал гасит action bar достижения")
    void masterChannelGatesReach() {

        display(true, false, false, false);

        Mockito.when(configManager.getReachSettings()).thenReturn(new ReachSettings(
                new MoonTitle("&6Navigator", "&fMark reached!", 0, 40, 10),
                new Messages(List.of("reached"), true),
                new Messages(List.of("bar"), true),
                Messages.disabled(),
                BarColor.GREEN, BarStyle.SOLID, 40L,
                null
        ));

        goalNotifier.notifyGoalReached(player, GOAL);

        Mockito.verify(messageService, Mockito.never()).sendActionBar(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));
        Mockito.verify(messageService).sendTitle(Mockito.eq(player), Mockito.any(MoonTitle.class), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Без звука достижение проходит тихо")
    void goalReachedWithoutSound() {

        Mockito.when(configManager.getReachSettings()).thenReturn(new ReachSettings(
                MoonTitle.disabled(),
                Messages.disabled(),
                Messages.disabled(),
                Messages.disabled(),
                BarColor.GREEN, BarStyle.SOLID, 40L,
                null
        ));

        goalNotifier.notifyGoalReached(player, GOAL);

        Mockito.verify(player, Mockito.never()).playSound(Mockito.any(org.bukkit.Location.class), Mockito.any(org.bukkit.Sound.class), Mockito.anyFloat(), Mockito.anyFloat());
        Mockito.verifyNoInteractions(bossBarService);

    }

    private void display(boolean title, boolean actionBar, boolean bossBar, boolean chat) {

        Mockito.when(configManager.getDisplaySettings()).thenReturn(new DisplaySettings(
                title, actionBar, bossBar, chat,
                new MoonTitle("{direction}", "sub", 0, 40, 10),
                new Messages(List.of("bar"), true),
                new Messages(List.of("{direction} {distance} bl. to {gps}"), true),
                new Messages(List.of("chat"), true),
                BarColor.BLUE, BarStyle.SOLID, BossBarProgress.DISTANCE
        ));

    }

    private Direction direction() {
        return new Direction(Direction.Relative.RIGHT, Direction.Compass.EAST);
    }
}
