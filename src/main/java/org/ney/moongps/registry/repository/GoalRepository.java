package org.ney.moongps.registry.repository;

import org.jetbrains.annotations.NotNull;
import org.ney.moongps.model.GPSGoal;

import java.util.List;

/**
 * Контракт хранилища меток.
 * Реализации: файл goals.yml и таблица MySQL.
 */
public interface GoalRepository {

    /**
     * Читает все метки из хранилища.
     *
     * @return список меток
     */
    @NotNull List<GPSGoal> loadAll();

    /**
     * Сохраняет метку (создаёт или обновляет).
     *
     * @param goal метка
     */
    void save(@NotNull GPSGoal goal);

    /**
     * Сохраняет все переданные метки одной операцией, где это возможно.
     *
     * @param goals метки
     */
    void saveAll(@NotNull List<GPSGoal> goals);

    /**
     * Удаляет метку из хранилища.
     *
     * @param goalName название метки
     */
    void delete(@NotNull String goalName);

    /**
     * Освобождает ресурсы хранилища.
     */
    void close();
}
