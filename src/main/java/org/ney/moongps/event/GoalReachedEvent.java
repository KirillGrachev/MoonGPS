package org.ney.moongps.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.model.GPSGoal;

/**
 * Событие достижения метки.
 * Вызывается в главном потоке после остановки навигатора.
 * Отмена события подавляет оповещение игрока (титул, звук, сообщения).
 */
public class GoalReachedEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final GPSGoal goal;
    private boolean cancelled;

    public GoalReachedEvent(@NotNull Player player, @NotNull GPSGoal goal) {

        super(player);
        this.goal = goal;

    }

    /**
     * Возвращает достигнутую метку.
     *
     * @return метка навигатора
     */
    public @NotNull GPSGoal getGoal() {
        return goal;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Вызывается Bukkit через отражение при регистрации слушателей,
     * в коде плагина напрямую не вызывается.
     *
     * @return общий список обработчиков события
     */
    @SuppressWarnings("unused")
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
