package org.ney.moongps.model;

import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigationSessionTest {

    private static final GPSGoal GOAL = new GPSGoal("shop", 1.0D, 2.0D, 3.0D, "lobby", null);

    @Test
    @DisplayName("Остановка сессии отменяет задачу")
    void stopCancelsTask() {

        NavigationSession session = new NavigationSession(UUID.randomUUID(), GOAL);
        BukkitTask task = Mockito.mock(BukkitTask.class);

        session.attachTask(task);

        assertTrue(session.stop());
        assertTrue(session.isStopped());

        Mockito.verify(task).cancel();

    }

    @Test
    @DisplayName("Повторная остановка не отменяет задачу второй раз")
    void stopIsIdempotent() {

        NavigationSession session = new NavigationSession(UUID.randomUUID(), GOAL);
        BukkitTask task = Mockito.mock(BukkitTask.class);

        session.attachTask(task);

        assertTrue(session.stop());
        assertFalse(session.stop());

        Mockito.verify(task, Mockito.times(1)).cancel();

    }

    @Test
    @DisplayName("Задача, добавленная после остановки, отменяется сразу")
    void taskAttachedAfterStopCancelled() {

        NavigationSession session = new NavigationSession(UUID.randomUUID(), GOAL);
        BukkitTask task = Mockito.mock(BukkitTask.class);

        session.stop();
        session.attachTask(task);

        Mockito.verify(task).cancel();

    }

    @Test
    @DisplayName("Метку сессии можно обновить без перезапуска задачи")
    void goalCanBeRefreshed() {

        NavigationSession session = new NavigationSession(UUID.randomUUID(), GOAL);
        GPSGoal updated = new GPSGoal("shop", 10.0D, 20.0D, 30.0D, "lobby", null);

        session.setGoal(updated);

        assertEquals(10.0D, session.getGoal().x());
        assertEquals("shop", session.getGoal().name());

    }
}
