package org.ney.moongps.service;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.support.BukkitSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalVisibilityServiceTest {

    private static final GPSGoal WORLD_GOAL = new GPSGoal("shop", 1.0D, 2.0D, 3.0D, "world", null);
    private static final GPSGoal OTHER_GOAL = new GPSGoal("mine", 1.0D, 2.0D, 3.0D, "nether", null);

    private ConfigManager configManager;
    private PermissionService permissionService;
    private GoalVisibilityService goalVisibilityService;
    private Player player;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        permissionService = Mockito.mock(PermissionService.class);

        goalVisibilityService = new GoalVisibilityService(configManager, permissionService);

        World world = BukkitSupport.world("world");
        player = Mockito.mock(Player.class);

        Mockito.when(player.getWorld()).thenReturn(world);
        Mockito.when(configManager.isOtherWorldRestricted()).thenReturn(true);
        Mockito.when(configManager.getOtherWorldPermission()).thenReturn("moongps.mark.other_world");

    }

    @Test
    @DisplayName("Метка своего мира видна всегда")
    void sameWorldVisible() {
        assertTrue(goalVisibilityService.isVisible(player, WORLD_GOAL));
    }

    @Test
    @DisplayName("Метка чужого мира без права скрыта")
    void otherWorldHidden() {

        Mockito.when(permissionService.hasPermission(player, "moongps.mark.other_world")).thenReturn(false);
        assertFalse(goalVisibilityService.isVisible(player, OTHER_GOAL));

    }

    @Test
    @DisplayName("Метка чужого мира с правом видна")
    void otherWorldVisibleWithPermission() {

        Mockito.when(permissionService.hasPermission(player, "moongps.mark.other_world")).thenReturn(true);
        assertTrue(goalVisibilityService.isVisible(player, OTHER_GOAL));

    }

    @Test
    @DisplayName("Выключенный тумблер показывает все метки")
    void restrictionDisabled() {

        Mockito.when(configManager.isOtherWorldRestricted()).thenReturn(false);
        assertTrue(goalVisibilityService.isVisible(player, OTHER_GOAL));

    }

    @Test
    @DisplayName("Консоль видит все метки")
    void consoleSeesAll() {

        CommandSender console = Mockito.mock(CommandSender.class);

        assertTrue(goalVisibilityService.isVisible(console, OTHER_GOAL));
        assertTrue(goalVisibilityService.isVisible(null, OTHER_GOAL));

    }

    @Test
    @DisplayName("Фильтр оставляет только видимые метки")
    void filterVisible() {

        Mockito.when(permissionService.hasPermission(player, "moongps.mark.other_world")).thenReturn(false);

        List<GPSGoal> visible = goalVisibilityService.filterVisible(player, List.of(WORLD_GOAL, OTHER_GOAL));

        assertEquals(List.of(WORLD_GOAL), visible);

    }
}
