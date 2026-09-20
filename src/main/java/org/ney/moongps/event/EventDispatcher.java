package org.ney.moongps.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.MoonGPS;

/**
 * Диспетчер регистрации слушателей событий.
 */
public class EventDispatcher {

    private final MoonGPS plugin;

    public EventDispatcher(@NotNull MoonGPS plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует переданные слушатели в Bukkit.
     *
     * @param listeners слушатели событий
     */
    public void registerEvents(Listener @NotNull ... listeners) {

        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }

    }
}
