package org.ney.moongps.command;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.MoonGPS;

import java.util.logging.Logger;

class CommandDispatcherTest {

    private MoonGPS plugin;
    private CommandDispatcher commandDispatcher;

    @BeforeEach
    void setUp() {

        plugin = Mockito.mock(MoonGPS.class);
        commandDispatcher = new CommandDispatcher(plugin);

    }

    @Test
    @DisplayName("Команда получает обработчик и автодополнение")
    void registersExecutor() {

        PluginCommand pluginCommand = Mockito.mock(PluginCommand.class);
        TabExecutor tabExecutor = Mockito.mock(TabExecutor.class);

        Mockito.when(plugin.getCommand("gps")).thenReturn(pluginCommand);

        commandDispatcher.registerCommand("gps", tabExecutor);

        Mockito.verify(pluginCommand).setExecutor(tabExecutor);
        Mockito.verify(pluginCommand).setTabCompleter(tabExecutor);

    }

    @Test
    @DisplayName("Отсутствующая в plugin.yml команда не роняет включение")
    void missingCommandLogged() {

        Mockito.when(plugin.getCommand("gps")).thenReturn(null);
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("MoonGPSTest"));

        commandDispatcher.registerCommand("gps", Mockito.mock(TabExecutor.class));

        Mockito.verify(plugin, Mockito.never()).getServer();

    }
}
