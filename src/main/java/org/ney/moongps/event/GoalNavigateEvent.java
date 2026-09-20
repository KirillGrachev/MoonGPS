package org.ney.moongps.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.model.GPSGoal;

/**
 * Событие включения навигатора до метки.
 * Вызывается в главном потоке перед запуском задачи навигации.
 * Отмена события предотвращает навигацию.
 */
public class GoalNavigateEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final GPSGoal goal;
    private boolean cancelled;

    public GoalNavigateEvent(@NotNull Player player, @NotNull GPSGoal goal) {

        super(player);
        this.goal = goal;

    }

    /**
     * Возвращает метку, до которой включается навигатор.
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
