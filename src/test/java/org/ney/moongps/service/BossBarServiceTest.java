package org.ney.moongps.service;

import org.bukkit.Server;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.ArgumentCaptor;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.type.BossBarProgress;
import org.ney.moongps.config.type.DisplaySettings;
import org.ney.moongps.support.BukkitSupport;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BossBarServiceTest {

    private MoonGPS plugin;
    private ConfigManager configManager;
    private BossBarService bossBarService;
    private BossBar bossBar;
    private Player player;

    @BeforeEach
    void setUp() {

        Server server = BukkitSupport.server();
        bossBar = Mockito.mock(BossBar.class);

        Mockito.when(server.createBossBar(Mockito.anyString(), Mockito.any(BarColor.class), Mockito.any(BarStyle.class)))
                .thenReturn(bossBar);

        plugin = Mockito.mock(MoonGPS.class);
        configManager = Mockito.mock(ConfigManager.class);

        Mockito.when(configManager.getDisplaySettings()).thenReturn(display(BossBarProgress.DISTANCE));
        Mockito.when(plugin.getServer()).thenReturn(BukkitSupport.server());

        bossBarService = new BossBarService(plugin, configManager);

        player = Mockito.mock(Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());

    }

    @Test
    @DisplayName("Первое обновление создаёт полосу и цепляет игрока")
    void updateCreatesBar() {

        bossBarService.update(player, "text", 100.0D);

        Mockito.verify(bossBar).addPlayer(player);
        Mockito.verify(bossBar).setVisible(true);
        Mockito.verify(bossBar).setTitle("text");
        assertEquals(1, bossBarService.getActiveBarsCount());

    }

    @Test
    @DisplayName("DISTANCE заполняет полосу по мере приближения")
    void distanceProgress() {

        bossBarService.update(player, "text", 100.0D);
        Mockito.verify(bossBar).setProgress(0.02D);

        bossBarService.update(player, "text", 50.0D);
        Mockito.verify(bossBar).setProgress(0.5D);

        bossBarService.update(player, "text", 0.0D);
        Mockito.verify(bossBar).setProgress(1.0D);

    }

    @Test
    @DisplayName("FULL держит полосу заполненной")
    void fullProgress() {

        Mockito.when(configManager.getDisplaySettings()).thenReturn(display(BossBarProgress.FULL));

        bossBarService.update(player, "text", 100.0D);
        bossBarService.update(player, "text", 10.0D);

        Mockito.verify(bossBar, Mockito.times(2)).setProgress(1.0D);

    }

    @Test
    @DisplayName("Снятие полосы убирает её с экрана")
    void removeDetaches() {

        bossBarService.update(player, "text", 100.0D);

        bossBarService.remove(player.getUniqueId());

        Mockito.verify(bossBar).removeAll();
        assertEquals(0, bossBarService.getActiveBarsCount());

    }

    @Test
    @DisplayName("removeAll снимает все полосы")
    void removeAllBars() {

        bossBarService.update(player, "text", 100.0D);

        bossBarService.removeAll();

        Mockito.verify(bossBar).removeAll();
        assertEquals(0, bossBarService.getActiveBarsCount());

    }

    @Test
    @DisplayName("flash показывает временную полосу и снимает её по таймеру")
    void flashTemporaryBar() {

        BukkitScheduler scheduler = Mockito.mock(BukkitScheduler.class);
        Mockito.when(BukkitSupport.server().getScheduler()).thenReturn(scheduler);

        bossBarService.flash(player, "reached", BarColor.GREEN, BarStyle.SOLID, 40L);

        Mockito.verify(bossBar).addPlayer(player);
        Mockito.verify(bossBar).setProgress(1.0D);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        Mockito.verify(scheduler).runTaskLater(Mockito.eq(plugin), captor.capture(), Mockito.eq(40L));

        captor.getValue().run();

        Mockito.verify(bossBar).removeAll();

    }

    private DisplaySettings display(BossBarProgress progress) {

        return new DisplaySettings(
                false, false, true, false,
                null, null, null, null,
                BarColor.BLUE, BarStyle.SOLID, progress
        );

    }
}
