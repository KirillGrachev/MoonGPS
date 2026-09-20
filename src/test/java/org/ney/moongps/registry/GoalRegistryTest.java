package org.ney.moongps.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalRegistryTest {

    private ConfigManager configManager;
    private GoalRegistry goalRegistry;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        Mockito.when(configManager.isCaseSensitive()).thenReturn(false);

        goalRegistry = new GoalRegistry(configManager);

    }

    @Test
    @DisplayName("Регистрация и поиск метки")
    void registerAndFind() {

        goalRegistry.registerGoal(goal("shop"));

        assertNotNull(goalRegistry.getGoal("shop"));
        assertTrue(goalRegistry.isGoalRegistered("shop"));
        assertEquals(1, goalRegistry.size());

    }

    @Test
    @DisplayName("Без учёта регистра названия совпадают")
    void caseInsensitive() {

        goalRegistry.registerGoal(goal("Shop"));
        assertNotNull(goalRegistry.getGoal("sHoP"));

    }

    @Test
    @DisplayName("С учётом регистра названия различаются")
    void caseSensitive() {

        Mockito.when(configManager.isCaseSensitive()).thenReturn(true);

        goalRegistry.registerGoal(goal("Shop"));

        assertNull(goalRegistry.getGoal("shop"));
        assertNotNull(goalRegistry.getGoal("Shop"));

    }

    @Test
    @DisplayName("Пустое название не даёт метки")
    void blankName() {

        assertNull(goalRegistry.getGoal(null));
        assertNull(goalRegistry.getGoal("  "));

    }

    @Test
    @DisplayName("Удаление возвращает удалённую метку")
    void removeReturnsGoal() {

        goalRegistry.registerGoal(goal("shop"));

        GPSGoal removed = goalRegistry.removeGoal("shop");

        assertNotNull(removed);
        assertEquals(0, goalRegistry.size());

    }

    @Test
    @DisplayName("Названия и метки сортируются по алфавиту")
    void sortedViews() {

        goalRegistry.registerGoal(goal("pvp"));
        goalRegistry.registerGoal(goal("bank"));
        goalRegistry.registerGoal(goal("shop"));

        assertEquals(List.of("bank", "pvp", "shop"), goalRegistry.getSortedGoalNames());
        assertEquals("bank", goalRegistry.getSortedGoals().get(0).name());

    }

    @Test
    @DisplayName("Очистка снимает все метки")
    void clear() {

        goalRegistry.registerGoal(goal("shop"));
        goalRegistry.clearGoals();

        assertEquals(0, goalRegistry.size());

    }

    private GPSGoal goal(String name) {
        return new GPSGoal(name, 0.0D, 0.0D, 0.0D, "lobby", null);
    }
}
