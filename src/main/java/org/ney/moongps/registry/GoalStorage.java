package org.ney.moongps.registry;

import org.jetbrains.annotations.NotNull;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.registry.repository.GoalRepository;

/**
 * Фасад над выбранным хранилищем меток.
 * Синхронизирует реестр в памяти с носителем (YAML или SQL).
 */
public class GoalStorage {

    private final GoalRegistry goalRegistry;
    private final GoalRepository goalRepository;

    public GoalStorage(@NotNull GoalRegistry goalRegistry, @NotNull GoalRepository goalRepository) {

        this.goalRegistry = goalRegistry;
        this.goalRepository = goalRepository;

    }

    /**
     * Загружает метки из хранилища в реестр.
     * Перед загрузкой реестр очищается.
     */
    public void loadGoals() {

        goalRegistry.clearGoals();
        goalRepository.loadAll().forEach(goalRegistry::registerGoal);

    }

    /**
     * Сохраняет одну метку в хранилище (создаёт или обновляет).
     *
     * @param goal метка
     */
    public void saveGoal(@NotNull GPSGoal goal) {
        goalRepository.save(goal);
    }

    /**
     * Удаляет метку из хранилища.
     *
     * @param goalName название метки
     */
    public void deleteGoal(@NotNull String goalName) {
        goalRepository.delete(goalName);
    }

    /**
     * Перезаписывает все метки реестра в хранилище.
     */
    public void saveGoals() {
        goalRepository.saveAll(goalRegistry.getSortedGoals());
    }

    /**
     * Освобождает ресурсы хранилища (при выключении плагина).
     */
    public void close() {
        goalRepository.close();
    }
}
