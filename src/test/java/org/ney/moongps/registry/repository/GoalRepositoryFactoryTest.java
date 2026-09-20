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

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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

    }

    @Test
    @DisplayName("Недоступная база роняет в YAML с предупреждением")
    void sqlFallback() {

        Mockito.when(configManager.getStorageSettings()).thenReturn(settings(StorageType.SQL));

        GoalRepository repository = new GoalRepositoryFactory(plugin, configManager).create();

        assertInstanceOf(YamlGoalRepository.class, repository);
        Mockito.verify(logger).severe(Mockito.contains("Falling back to YAML"));

    }

    private StorageSettings settings(StorageType type) {

        SqlSettings sql = new SqlSettings(
                "127.0.0.1", 1, "moongps", "moongps_marks", "root", "", Map.of()
        );
        return new StorageSettings(type, sql);

    }
}
