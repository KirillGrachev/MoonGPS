package org.ney.moongps.registry.repository;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.util.GoalNameValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Хранилище меток в файле goals.yml.
 */
public class YamlGoalRepository implements GoalRepository {

    private static final String FILE_NAME = "goals.yml";
    private static final String SECTION_MARKS = "marks";

    private final MoonGPS plugin;
    private final File goalsFile;

    public YamlGoalRepository(@NotNull MoonGPS plugin) {

        this.plugin = plugin;
        this.goalsFile = new File(plugin.getDataFolder(), FILE_NAME);

        createFileIfNotExists();

    }

    private void createFileIfNotExists() {

        if (goalsFile.exists()) return;
        plugin.saveResource(FILE_NAME, false);

    }

    @Override
    public @NotNull List<GPSGoal> loadAll() {

        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(goalsFile);
        ConfigurationSection marksSection = fileConfiguration.getConfigurationSection(SECTION_MARKS);

        List<GPSGoal> goals = new ArrayList<>();

        if (marksSection == null) {

            plugin.getLogger().warning("Section 'marks' not found in goals.yml - no marks loaded");
            return goals;

        }

        for (String goalKey : marksSection.getKeys(false)) {

            GPSGoal goal = readGoal(marksSection.getConfigurationSection(goalKey), goalKey);

            if (goal == null) {

                plugin.getLogger().warning("Mark '" + goalKey + "' in goals.yml is invalid and has been skipped");
                continue;

            }

            goals.add(goal);

        }

        return goals;

    }

    @Override
    public void save(@NotNull GPSGoal goal) {

        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(goalsFile);
        ConfigurationSection marksSection = marksSection(fileConfiguration);

        removeDuplicates(marksSection, goal.name());
        writeGoal(marksSection, goal);
        saveFile(fileConfiguration);

    }

    @Override
    public void saveAll(@NotNull List<GPSGoal> goals) {

        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(goalsFile);
        ConfigurationSection marksSection = fileConfiguration.createSection(SECTION_MARKS);

        goals.forEach(goal -> writeGoal(marksSection, goal));
        saveFile(fileConfiguration);

    }

    @Override
    public void delete(@NotNull String goalName) {

        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(goalsFile);
        ConfigurationSection marksSection = fileConfiguration.getConfigurationSection(SECTION_MARKS);

        if (marksSection == null) return;

        marksSection.set(goalName, null);
        saveFile(fileConfiguration);

    }

    @Override
    public void close() {
        // Файл не держит открытых ресурсов
    }

    /* Чтение и запись файла */

    private @Nullable GPSGoal readGoal(@Nullable ConfigurationSection goalSection, @NotNull String goalKey) {

        if (goalSection == null) return null;
        if (!GoalNameValidator.isValid(goalKey)) return null;

        if (!goalSection.contains("x") || !goalSection.contains("y") || !goalSection.contains("z")) {
            return null;
        }

        String world = goalSection.getString("world");

        if (world == null || world.isBlank()) {
            world = getDefaultWorldName();
        }

        String permission = goalSection.getString("permission");

        return new GPSGoal(
                goalKey,
                goalSection.getDouble("x"),
                goalSection.getDouble("y"),
                goalSection.getDouble("z"),
                world,
                permission == null || permission.isBlank() ? null : permission.trim()
        );

    }

    private void writeGoal(@NotNull ConfigurationSection marksSection, @NotNull GPSGoal goal) {

        ConfigurationSection goalSection = marksSection.createSection(goal.name());

        goalSection.set("x", round(goal.x()));
        goalSection.set("y", round(goal.y()));
        goalSection.set("z", round(goal.z()));
        goalSection.set("world", goal.world());

        if (goal.permission() != null) {
            goalSection.set("permission", goal.permission());
        }

    }

    private @NotNull ConfigurationSection marksSection(@NotNull FileConfiguration fileConfiguration) {

        ConfigurationSection marksSection = fileConfiguration.getConfigurationSection(SECTION_MARKS);

        if (marksSection == null) {
            marksSection = fileConfiguration.createSection(SECTION_MARKS);
        }

        return marksSection;

    }

    /**
     * Удаляет из секции ключи, совпадающие с названием метки без учёта регистра.
     *
     * @param marksSection секция с метками
     * @param goalName     название метки
     */
    private void removeDuplicates(@NotNull ConfigurationSection marksSection, @NotNull String goalName) {

        String normalizedName = goalName.toLowerCase(Locale.ROOT);
        marksSection.getKeys(false).stream()
                .filter(key -> key.toLowerCase(Locale.ROOT).equals(normalizedName))
                .toList()
                .forEach(key -> marksSection.set(key, null));

    }

    /**
     * Возвращает название основного мира сервера.
     *
     * @return название мира или "world", если миры ещё не загружены
     */
    private @NotNull String getDefaultWorldName() {

        return Bukkit.getWorlds().stream()
                .findFirst()
                .map(world -> world.getName())
                .orElse("world");

    }

    private double round(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }

    private void saveFile(@NotNull FileConfiguration fileConfiguration) {

        try {
            fileConfiguration.save(goalsFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save " + FILE_NAME + ": " + exception.getMessage());
        }

    }
}
