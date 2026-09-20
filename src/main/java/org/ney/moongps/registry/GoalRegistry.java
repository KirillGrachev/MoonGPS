package org.ney.moongps.registry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.GPSGoal;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр меток навигатора.
 * Хранит метки в памяти и учитывает настройку чувствительности к регистру.
 */
public class GoalRegistry {

    private final ConfigManager configManager;
    private final Map<String, GPSGoal> registeredGoals = new ConcurrentHashMap<>();

    public GoalRegistry(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    private @NotNull String normalizeGoalName(@NotNull String name) {
        return configManager.isCaseSensitive() ? name : name.toLowerCase(Locale.ROOT);
    }

    /**
     * Регистрирует метку.
     *
     * @param goal метка навигатора
     */
    public void registerGoal(@NotNull GPSGoal goal) {
        registeredGoals.put(normalizeGoalName(goal.name()), goal);
    }

    /**
     * Возвращает метку по названию.
     *
     * @param goalName название метки
     * @return метка или null, если она не найдена
     */
    public @Nullable GPSGoal getGoal(@Nullable String goalName) {

        if (goalName == null || goalName.isBlank()) return null;
        return registeredGoals.get(normalizeGoalName(goalName));

    }

    /**
     * Удаляет метку из реестра.
     *
     * @param goalName название метки
     * @return удалённая метка или null
     */
    public @Nullable GPSGoal removeGoal(@NotNull String goalName) {
        return registeredGoals.remove(normalizeGoalName(goalName));
    }

    public boolean isGoalRegistered(@NotNull String goalName) {
        return registeredGoals.containsKey(normalizeGoalName(goalName));
    }

    public void clearGoals() {
        registeredGoals.clear();
    }

    public int size() {
        return registeredGoals.size();
    }

    /**
     * Возвращает названия всех меток, отсортированные по алфавиту.
     *
     * @return отсортированный список названий
     */
    public @NotNull List<String> getSortedGoalNames() {

        return registeredGoals.values().stream()
                .map(GPSGoal::name)
                .sorted(Comparator.naturalOrder())
                .toList();

    }

    /**
     * Возвращает все метки, отсортированные по названию.
     *
     * @return отсортированная коллекция меток
     */
    public @NotNull List<GPSGoal> getSortedGoals() {

        return registeredGoals.values().stream()
                .sorted(Comparator.comparing(GPSGoal::name))
                .toList();

    }

    /**
     * Собирает права, уже используемые метками (для автодополнения).
     *
     * @return отсортированные названия прав без повторов
     */
    public @NotNull List<String> getUsedPermissions() {

        return registeredGoals.values().stream()
                .map(GPSGoal::permission)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

    }
}
