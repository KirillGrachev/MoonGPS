package org.ney.moongps.event;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.model.GPSGoal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalEventsTest {

    private static final GPSGoal GOAL = new GPSGoal("shop", 1.0D, 2.0D, 3.0D, "lobby", null);

    private Player player;

    @BeforeEach
    void setUp() {
        player = Mockito.mock(Player.class);
    }

    @Test
    @DisplayName("GoalNavigateEvent хранит игрока и метку, отменяется")
    void navigateEvent() {

        GoalNavigateEvent event = new GoalNavigateEvent(player, GOAL);

        assertSame(player, event.getPlayer());
        assertEquals(GOAL, event.getGoal());
        assertFalse(event.isCancelled());

        event.setCancelled(true);

        assertTrue(event.isCancelled());
        assertSame(event.getHandlers(), GoalNavigateEvent.getHandlerList());

    }

    @Test
    @DisplayName("GoalReachedEvent хранит игрока и метку, отменяется")
    void reachedEvent() {

        GoalReachedEvent event = new GoalReachedEvent(player, GOAL);

        assertSame(player, event.getPlayer());
        assertEquals(GOAL, event.getGoal());
        assertFalse(event.isCancelled());

        event.setCancelled(true);

        assertTrue(event.isCancelled());
        assertSame(event.getHandlers(), GoalReachedEvent.getHandlerList());

    }
}
