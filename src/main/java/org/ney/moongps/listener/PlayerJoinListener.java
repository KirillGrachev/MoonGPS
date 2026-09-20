package org.ney.moongps.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.service.NavigationService;

/**
 * Слушатель входа игрока.
 * Запускает автонавигацию до метки из settings.navigation.auto_start:
 * на включении плагина игроков обычно ещё нет, поэтому точка входа - join.
 */
public class PlayerJoinListener implements Listener {

    private final ConfigManager configManager;
    private final NavigationService navigationService;

    public PlayerJoinListener(@NotNull ConfigManager configManager,
                              @NotNull NavigationService navigationService) {

        this.configManager = configManager;
        this.navigationService = navigationService;

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {

        String autoStartGoal = configManager.getAutoStartGoal();

        if (autoStartGoal.isEmpty()) return;

        navigationService.toggleGoal(event.getPlayer(), autoStartGoal, false);

    }
}
