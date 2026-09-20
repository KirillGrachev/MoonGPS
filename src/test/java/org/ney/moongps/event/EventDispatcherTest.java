package org.ney.moongps.event;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.support.BukkitSupport;

class EventDispatcherTest {

    private MoonGPS plugin;
    private PluginManager pluginManager;
    private EventDispatcher eventDispatcher;

    @BeforeEach
    void setUp() {

        plugin = Mockito.mock(MoonGPS.class);
        pluginManager = BukkitSupport.newPluginManager();

        eventDispatcher = new EventDispatcher(plugin);

    }

    @Test
    @DisplayName("Все переданные слушатели регистрируются в Bukkit")
    void registersListeners() {

        Listener first = Mockito.mock(Listener.class);
        Listener second = Mockito.mock(Listener.class);

        eventDispatcher.registerEvents(first, second);

        Mockito.verify(pluginManager).registerEvents(first, plugin);
        Mockito.verify(pluginManager).registerEvents(second, plugin);

    }
}
