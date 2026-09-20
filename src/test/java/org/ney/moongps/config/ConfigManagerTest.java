package org.ney.moongps.config;

import org.bukkit.Sound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.type.DirectionMode;
import org.ney.moongps.config.type.StorageType;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверка разбора реального config.yml из ресурсов плагина.
 */
class ConfigManagerTest {

    @TempDir
    Path dataFolder;

    private ConfigManager configManager;

    @BeforeEach
    void setUp() throws IOException {

        try (InputStream configStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {

            assertNotNull(configStream, "config.yml не найден в ресурсах");
            Files.copy(configStream, dataFolder.resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING);

        }

        MoonGPS plugin = Mockito.mock(MoonGPS.class);

        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("MoonGPSTest"));

        configManager = new ConfigManager(plugin);

    }

    @Test
    @DisplayName("Основные настройки читаются из файла")
    void baseSettingsLoaded() {

        assertTrue(configManager.isNavigatorEnabled());
        assertFalse(configManager.arePermissionsEnabled());
        assertFalse(configManager.isCaseSensitive());
        assertEquals(8L, configManager.getNavigationInterval());
        assertEquals(2.0D, configManager.getReachDistance());
        assertTrue(configManager.shouldStopOnWorldChange());
        assertEquals("", configManager.getAutoStartGoal());

    }

    @Test
    @DisplayName("Список миров и права читаются из файла")
    void worldsAndPermissionsLoaded() {

        assertTrue(configManager.areWorldsRestricted());
        assertTrue(configManager.getAllowedWorlds().contains("world"));
        assertEquals("moongps.use", configManager.getPermissionUse());
        assertEquals("moongps.reload", configManager.getPermissionReload());

    }

    @Test
    @DisplayName("Настройки направления читаются из файла")
    void directionSettingsLoaded() {

        assertEquals(DirectionMode.RELATIVE, configManager.getDirectionSettings().mode());
        assertEquals(0.85D, configManager.getDirectionSettings().aheadThreshold());
        assertEquals(-0.85D, configManager.getDirectionSettings().behindThreshold());
        assertTrue(configManager.getDirectionSettings().compassSymbols().containsKey("NORTH"));
        assertTrue(configManager.getDirectionSettings().relativeNames().containsKey("AHEAD"));

    }

    @Test
    @DisplayName("Настройки вывода и достижения метки читаются из файла")
    void displayAndReachSettingsLoaded() {

        assertTrue(configManager.getDisplaySettings().titleEnabled());
        assertFalse(configManager.getDisplaySettings().actionBarEnabled());
        assertFalse(configManager.getDisplaySettings().bossBarEnabled());
        assertTrue(configManager.getDisplaySettings().title().isEnabled());
        assertFalse(configManager.getDisplaySettings().bossBar().isEmpty());
        assertNotNull(configManager.getReachSettings().sound());
        assertEquals(Sound.ENTITY_PLAYER_LEVELUP, configManager.getReachSettings().sound().sound());

    }

    @Test
    @DisplayName("Сообщения кэшируются с префиксом и цветовыми кодами")
    void messagesLoaded() {

        assertTrue(configManager.getPrefix().contains("§"));
        assertFalse(configManager.getUsageMessage().isEmpty());
        assertTrue(configManager.getEnabledMessage().values().get(0).contains("{gps}"));
        assertTrue(configManager.getListEntryFormat().values().get(0).contains("{gps}"));
        assertEquals("§6Navigator", configManager.getReachSettings().title().title());

    }

    @Test
    @DisplayName("Сообщение о перезагрузке содержит плейсхолдер меток")
    void reloadMessageHasMarksPlaceholder() {
        assertTrue(configManager.getReloadSuccessMessage().values().get(0).contains("{marks}"));
    }

    @Test
    @DisplayName("Битые значения конфига заменяются безопасными")
    void brokenValuesFallBack() throws IOException {

        ConfigManager edgeManager = load("config-edge.yml");

        assertEquals(DirectionMode.RELATIVE, edgeManager.getDirectionSettings().mode());
        assertTrue(edgeManager.getDisplaySettings().titleEnabled());
        assertEquals(BarColor.BLUE, edgeManager.getDisplaySettings().barColor());
        assertEquals(BarStyle.SOLID, edgeManager.getDisplaySettings().barStyle());
        assertEquals(1.0D, edgeManager.getDirectionSettings().aheadThreshold());
        assertEquals(-1.0D, edgeManager.getDirectionSettings().behindThreshold());
        assertEquals(1L, edgeManager.getNavigationInterval());
        assertEquals(0.1D, edgeManager.getReachDistance());
        assertNull(edgeManager.getReachSettings().sound());
        assertTrue(edgeManager.getUsageMessage().isEmpty());
        assertEquals(10, edgeManager.getListPerPage());

    }

    private ConfigManager load(String resourceName) throws IOException {

        Path folder = Files.createTempDirectory("moongps-edge");

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(stream, folder.resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING);
        }

        MoonGPS edgePlugin = Mockito.mock(MoonGPS.class);

        Mockito.when(edgePlugin.getDataFolder()).thenReturn(folder.toFile());
        Mockito.when(edgePlugin.getLogger()).thenReturn(Logger.getLogger("MoonGPSTest"));

        return new ConfigManager(edgePlugin);

    }

    @Test
    @DisplayName("Хранилище читается вместе с настройками SQL")
    void storageLoaded() {

        assertEquals(StorageType.YAML, configManager.getStorageSettings().type());
        assertEquals("localhost", configManager.getStorageSettings().sql().host());
        assertEquals(3306, configManager.getStorageSettings().sql().port());
        assertEquals("moongps_marks", configManager.getStorageSettings().sql().table());
        assertEquals("false", configManager.getStorageSettings().sql().properties().get("useSSL"));

    }

    @Test
    @DisplayName("Префикс подставляется только через переменную {prefix}")
    void prefixIsUsedAsVariable() {

        assertTrue(configManager.getEnabledMessage().values().get(0).contains("{prefix}"));
        assertTrue(configManager.getMarkNotFoundMessage().values().get(0).contains("{prefix}"));

        // Титулы и action bar префикс не получают
        assertFalse(configManager.getDisplaySettings().title().title().contains("{prefix}"));
        assertFalse(configManager.getDisplaySettings().actionBar().values().get(0).contains("{prefix}"));

    }
}
