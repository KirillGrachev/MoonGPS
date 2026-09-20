package org.ney.moongps.model;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ney.moongps.support.BukkitSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GPSGoalTest {

    @BeforeEach
    void setUp() {
        BukkitSupport.clearWorlds();
    }

    @Test
    @DisplayName("Метка собирается из локации игрока")
    void fromLocation() {

        World world = BukkitSupport.world("world");
        Location location = new Location(world, 1.5D, 2.5D, 3.5D);

        GPSGoal goal = GPSGoal.of("shop", location, "moongps.mark.shop");

        assertEquals("shop", goal.name());
        assertEquals(1.5D, goal.x());
        assertEquals("world", goal.world());
        assertEquals("moongps.mark.shop", goal.permission());

    }

    @Test
    @DisplayName("Локации без мира соответствует мир world")
    void locationWithoutWorld() {

        GPSGoal goal = GPSGoal.of("shop", new Location(null, 0.0D, 0.0D, 0.0D), null);

        assertEquals("world", goal.world());
        assertNull(goal.permission());

    }

    @Test
    @DisplayName("Локация собирается только для загруженного мира")
    void toLocation() {

        World world = BukkitSupport.world("world");

        GPSGoal goal = new GPSGoal("shop", 1.0D, 2.0D, 3.0D, "world", null);

        assertTrue(goal.isWorldLoaded());

        Location location = goal.toLocation();

        assertNotNull(location);
        assertEquals(world, location.getWorld());
        assertEquals(2.0D, location.getY());

    }

    @Test
    @DisplayName("Незагруженный мир не даёт локации")
    void toLocationWithoutWorld() {

        GPSGoal goal = new GPSGoal("shop", 1.0D, 2.0D, 3.0D, "missing", null);

        assertFalse(goal.isWorldLoaded());
        assertNull(goal.toLocation());

    }
}
