package org.ney.moongps;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Сквозной тест плагина на MockBukkit: настоящее включение,
 * команды, слушатели и выключение.
 */
class MoonGPSPluginTest {

    private ServerMock server;
    private MoonGPS plugin;

    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        server.addSimpleWorld("world");

        plugin = MockBukkit.load(MoonGPS.class);

    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Плагин включается, метки и команда на месте")
    void pluginLoads() {

        assertTrue(plugin.isEnabled());
        assertEquals(2, plugin.getGoalRegistry().size());
        assertNotNull(server.getPluginCommand("gps").getExecutor());
        assertNotNull(server.getPluginCommand("gps").getTabCompleter());

    }

    @Test
    @DisplayName("Команда list показывает метки игроку")
    void listCommand() {

        PlayerMock player = server.addPlayer();

        player.performCommand("gps list");

        StringBuilder output = new StringBuilder();
        String line;

        while ((line = player.nextMessage()) != null) {
            output.append(line);
        }

        assertTrue(output.toString().contains("shop"));

    }

    @Test
    @DisplayName("Навигатор включается и выключается повторной командой")
    void toggleNavigation() {

        PlayerMock player = server.addPlayer();

        player.performCommand("gps shop");

        assertNotNull(plugin.getNavigationService().getActiveGoal(player));

        player.performCommand("gps shop");

        assertNull(plugin.getNavigationService().getActiveGoal(player));

    }

    @Test
    @DisplayName("Выход игрока снимает навигатор")
    void quitStopsNavigation() {

        PlayerMock player = server.addPlayer();

        player.performCommand("gps shop");

        player.disconnect();

        assertEquals(0, plugin.getNavigationService().getActiveSessionsCount());

    }

    @Test
    @DisplayName("Перезагрузка не роняет плагин и сохраняет метки")
    void reloadCommand() {

        PlayerMock player = server.addPlayer();

        player.performCommand("gps reload");

        assertEquals(2, plugin.getGoalRegistry().size());

    }

    @Test
    @DisplayName("Автонавигация включается при входе игрока")
    void autoStartOnJoin() throws java.io.IOException {

        String config;

        try (java.io.InputStream stream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            config = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }

        config = config.replace("auto_start: \"\"", "auto_start: \"shop\"");

        java.nio.file.Files.write(
                plugin.getDataFolder().toPath().resolve("config.yml"),
                config.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        plugin.reloadPlugin();

        PlayerMock joined = server.addPlayer();

        assertEquals("shop", plugin.getNavigationService().getActiveGoal(joined).name());

    }

    @Test
    @DisplayName("Выключение плагина проходит чисто")
    void pluginDisables() {

        plugin.onDisable();
        assertEquals(0, plugin.getNavigationService().getActiveSessionsCount());

    }
}
