package org.ney.moongps.registry;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.registry.repository.YamlGoalRepository;
import org.ney.moongps.support.BukkitSupport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalStorageTest {

    @TempDir
    Path dataFolder;

    private MoonGPS plugin;
    private GoalRegistry goalRegistry;
    private GoalStorage goalStorage;

    @BeforeEach
    void setUp() throws IOException {

        copyResource("direction-config.yml", "config.yml");
        copyResource("goals-test.yml", "goals.yml");

        plugin = Mockito.mock(MoonGPS.class);

        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("MoonGPSTest"));

        goalRegistry = new GoalRegistry(new ConfigManager(plugin));
        goalStorage = new GoalStorage(goalRegistry, new YamlGoalRepository(plugin));

    }

    @Test
    @DisplayName("Метки загружаются из goals.yml вместе с правом доступа")
    void goalsLoaded() {

        goalStorage.loadGoals();

        assertEquals(2, goalRegistry.size());

        GPSGoal bank = goalRegistry.getGoal("bank");

        assertNotNull(bank);
        assertEquals(19.5D, bank.x());
        assertEquals("world", bank.world());
        assertEquals("moongps.mark.bank", bank.permission());
        assertNull(goalRegistry.getGoal("shop").permission());

    }

    @Test
    @DisplayName("Название метки ищется без учёта регистра")
    void goalNameIsCaseInsensitive() {

        goalStorage.loadGoals();

        assertNotNull(goalRegistry.getGoal("SHOP"));
        assertNotNull(goalRegistry.getGoal("Bank"));

    }

    @Test
    @DisplayName("Битые метки пропускаются, остальные загружаются")
    void brokenGoalsSkipped() throws IOException {

        copyResource("goals-broken.yml", "goals.yml");

        goalStorage.loadGoals();

        assertEquals(1, goalRegistry.size());
        assertNotNull(goalRegistry.getGoal("shop"));
        assertNull(goalRegistry.getGoal("incomplete"));

    }

    @Test
    @DisplayName("Новая метка сохраняется в goals.yml и переживает перезагрузку")
    void goalSavedAndReloaded() {

        goalStorage.loadGoals();

        World world = Mockito.mock(World.class);
        Mockito.when(world.getName()).thenReturn("world");

        GPSGoal pvp = GPSGoal.of("pvp", new Location(world, 3.5D, 108.0D, 235.5D), null);

        goalRegistry.registerGoal(pvp);
        goalStorage.saveGoal(pvp);
        goalStorage.loadGoals();

        assertEquals(3, goalRegistry.size());

        GPSGoal reloaded = goalRegistry.getGoal("pvp");

        assertNotNull(reloaded);
        assertEquals(3.5D, reloaded.x());
        assertEquals(108.0D, reloaded.y());
        assertEquals(235.5D, reloaded.z());
        assertEquals("world", reloaded.world());

    }

    @Test
    @DisplayName("Обновление метки не создаёт дубль в goals.yml")
    void goalUpdatedWithoutDuplicate() {

        goalStorage.loadGoals();

        World world = Mockito.mock(World.class);
        Mockito.when(world.getName()).thenReturn("world");

        GPSGoal shop = GPSGoal.of("shop", new Location(world, 1.0D, 2.0D, 3.0D), null);

        goalRegistry.registerGoal(shop);
        goalStorage.saveGoal(shop);
        goalStorage.loadGoals();

        assertEquals(2, goalRegistry.size());
        assertEquals(1.0D, goalRegistry.getGoal("shop").x());

    }

    @Test
    @DisplayName("Удаление метки убирает её из goals.yml")
    void goalDeleted() {

        goalStorage.loadGoals();

        GPSGoal removed = goalRegistry.removeGoal("bank");

        assertNotNull(removed);
        goalStorage.deleteGoal(removed.name());
        goalStorage.loadGoals();

        assertEquals(1, goalRegistry.size());
        assertNull(goalRegistry.getGoal("bank"));
        assertTrue(goalRegistry.getSortedGoalNames().contains("shop"));

    }

    @Test
    @DisplayName("Сохранение всех меток перезаписывает goals.yml")
    void allGoalsSaved() {

        goalStorage.loadGoals();
        goalRegistry.removeGoal("bank");
        goalStorage.saveGoals();
        goalStorage.loadGoals();

        assertEquals(1, goalRegistry.size());
        assertFalse(goalRegistry.isGoalRegistered("bank"));

    }

    @Test
    @DisplayName("Файл без секции marks даёт пустой реестр")
    void missingMarksSection() throws IOException {

        Files.writeString(dataFolder.resolve("goals.yml"), "# empty file\n");

        goalStorage.loadGoals();

        assertEquals(0, goalRegistry.size());

    }

    @Test
    @DisplayName("Метка без мира получает основной мир сервера")
    void defaultWorldApplied() throws IOException {

        Files.writeString(dataFolder.resolve("goals.yml"), ""
                + "marks:\n"
                + "  spawn:\n"
                + "    x: 0.0\n"
                + "    y: 70.0\n"
                + "    z: 0.0\n");

        Server server = BukkitSupport.server();
        World lobby = BukkitSupport.world("world");

        Mockito.when(server.getWorlds()).thenReturn(List.of(lobby));

        goalStorage.loadGoals();

        assertEquals("world", goalRegistry.getGoal("spawn").world());

    }

    @Test
    @DisplayName("Удаление из файла без секции marks не падает")
    void deleteWithoutSection() throws IOException {

        Files.writeString(dataFolder.resolve("goals.yml"), "# empty file\n");

        goalStorage.deleteGoal("shop");

        assertEquals(0, goalRegistry.size());

    }

    private void copyResource(String resourceName, String fileName) throws IOException {

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName)) {

            assertNotNull(stream, "Ресурс не найден: " + resourceName);
            Files.copy(stream, dataFolder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        }

    }
}
