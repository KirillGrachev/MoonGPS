package org.ney.moongps.model;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Активная сессия навигации игрока до метки.
 * Хранит задачу обновления и флаг остановки,
 * чтобы задача могла безопасно завершить себя из асинхронного потока.
 */
public class NavigationSession {

    private final UUID playerUUID;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    private volatile GPSGoal goal;

    private volatile BukkitTask task;

    public NavigationSession(@NotNull UUID playerUUID, @NotNull GPSGoal goal) {

        this.playerUUID = playerUUID;
        this.goal = goal;

    }

    /**
     * Привязывает задачу обновления к сессии.
     * Если сессия уже остановлена - задача отменяется сразу.
     *
     * @param bukkitTask задача обновления навигации
     */
    public void attachTask(@NotNull BukkitTask bukkitTask) {

        this.task = bukkitTask;
        if (stopped.get()) {
            bukkitTask.cancel();
        }

    }

    /**
     * Останавливает сессию и отменяет задачу обновления.
     * Метод потокобезопасен.
     *
     * @return true если сессия была активной
     */
    public boolean stop() {

        if (!stopped.compareAndSet(false, true)) {
            return false;
        }

        BukkitTask currentTask = this.task;
        if (currentTask != null) {
            currentTask.cancel();
        }

        return true;

    }

    public boolean isStopped() {
        return stopped.get();
    }

    public @NotNull UUID getPlayerUUID() {
        return playerUUID;
    }

    public @NotNull GPSGoal getGoal() {
        return goal;
    }

    /**
     * Обновляет метку сессии (например, после перезагрузки координат).
     *
     * @param goal новая метка
     */
    public void setGoal(@NotNull GPSGoal goal) {
        this.goal = goal;
    }

    /**
     * Возвращает игрока сессии.
     *
     * @return игрок или null, если он вышел с сервера
     */
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }
}
