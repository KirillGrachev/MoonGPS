package org.ney.moongps.registry.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.type.SqlSettings;
import org.ney.moongps.config.type.StorageSettings;
import org.ney.moongps.config.type.StorageType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalRepositoryFactoryTest {

    @TempDir
    Path dataFolder;

    private MoonGPS plugin;
    private ConfigManager configManager;
    private Logger logger;

    @BeforeEach
    void setUp() {

        plugin = Mockito.mock(MoonGPS.class);
        configManager = Mockito.mock(ConfigManager.class);

        logger = Mockito.mock(Logger.class);

        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        Mockito.when(plugin.getLogger()).thenReturn(logger);

    }

    @Test
    @DisplayName("Тип YAML даёт файловое хранилище")
    void yamlSelected() {

        Mockito.when(configManager.getStorageSettings()).thenReturn(settings(StorageType.YAML));

        GoalRepository repository = new GoalRepositoryFactory(plugin, configManager).create();

        assertInstanceOf(YamlGoalRepository.class, repository);
        assertEquals(StorageType.YAML, repository.type());

    }

    @Test
    @DisplayName("Тип FILE создаёт базу в папке плагина")
    void fileSelected() throws Exception {

        // jar драйвера кладём в libs вручную, чтобы тест не ходил в сеть
        Path libs = dataFolder.resolve("libs");
        Files.createDirectories(libs);
        Files.copy(sourceJar(), libs.resolve("h2-2.2.224.jar"));

        Mockito.when(configManager.isDownloadLibrariesEnabled()).thenReturn(true);
        Mockito.when(configManager.getStorageSettings()).thenReturn(settings(StorageType.FILE));

        GoalRepository repository = new GoalRepositoryFactory(plugin, configManager).create();

        assertInstanceOf(SqlGoalRepository.class, repository);
        assertEquals(StorageType.FILE, repository.type());
        assertTrue(Files.exists(dataFolder.resolve("storage/moongps.mv.db")));

        repository.close();

    }

    @Test
    @DisplayName("Недоступная база роняет в YAML с предупреждением")
    void sqlFallback() {

        // скачивание выключено и jar нет - фолбэк без похода в сеть
        Mockito.when(configManager.isDownloadLibrariesEnabled()).thenReturn(false);
        Mockito.when(configManager.getStorageSettings()).thenReturn(settings(StorageType.MYSQL));

        GoalRepository repository = new GoalRepositoryFactory(plugin, configManager).create();

        assertInstanceOf(YamlGoalRepository.class, repository);
        Mockito.verify(logger).severe(Mockito.contains("Falling back to YAML"));

    }

    private Path sourceJar() throws Exception {

        return java.nio.file.Paths.get(
                org.h2.Driver.class.getProtectionDomain().getCodeSource().getLocation().toURI()
        );

    }

    private StorageSettings settings(StorageType type) {

        SqlSettings sql = new SqlSettings(
                "127.0.0.1", 1, "moongps", "moongps_marks", "root", "", Map.of()
        );
        return new StorageSettings(type, sql);

    }
}
