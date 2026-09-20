package org.ney.moongps.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.service.NavigationService;

/**
 * Слушатель выхода игрока с сервера.
 * Останавливает навигатор и снимает задачу обновления,
 * чтобы задачи и сессии не оставались в памяти.
 */
public class PlayerQuitListener implements Listener {

    private final NavigationService navigationService;

    public PlayerQuitListener(@NotNull NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {

        Player player = event.getPlayer();
        navigationService.stopNavigation(player, false);

    }
}
