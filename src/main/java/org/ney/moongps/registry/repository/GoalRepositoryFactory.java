package org.ney.moongps.registry.repository;

import org.jetbrains.annotations.NotNull;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.type.StorageSettings;
import org.ney.moongps.config.type.StorageType;
import org.ney.moongps.model.GPSGoal;

import java.sql.SQLException;
import java.util.List;

/**
 * Фабрика хранилища меток по выбранному в конфиге формату.
 * При недоступности MySQL плагин продолжает работу на YAML
 * и пишет об этом в лог жирным предупреждением.
 */
public class GoalRepositoryFactory {

    private final MoonGPS plugin;
    private final ConfigManager configManager;

    public GoalRepositoryFactory(@NotNull MoonGPS plugin, @NotNull ConfigManager configManager) {

        this.plugin = plugin;
        this.configManager = configManager;

    }

    /**
     * Создаёт хранилище выбранного типа.
     *
     * @return готовое к работе хранилище
     */
    public @NotNull GoalRepository create() {

        StorageSettings settings = configManager.getStorageSettings();
        YamlGoalRepository yamlRepository = new YamlGoalRepository(plugin);

        if (settings.type() == StorageType.YAML) {
            return yamlRepository;
        }

        try {

            SqlGoalRepository sqlRepository = createSqlRepository(settings);

            importFromYamlIfEmpty(sqlRepository, yamlRepository);

            return sqlRepository;

        } catch (SQLException exception) {

            plugin.getLogger().severe("SQL storage is unavailable: " + exception.getMessage()
                    + ". Falling back to YAML (goals.yml).");
            return yamlRepository;

        }

    }

    private @NotNull SqlGoalRepository createSqlRepository(@NotNull StorageSettings settings) throws SQLException {

        SqlGoalRepository sqlRepository = new SqlGoalRepository(
                new MysqlConnectionFactory(settings.sql()),
                settings.sql(),
                plugin.getLogger()
        );

        sqlRepository.connect();

        return sqlRepository;

    }

    /**
     * Переносит метки из goals.yml в пустую таблицу при первом включении SQL.
     *
     * @param sqlRepository  хранилище MySQL
     * @param yamlRepository хранилище YAML
     */
    private void importFromYamlIfEmpty(@NotNull SqlGoalRepository sqlRepository,
                                       @NotNull YamlGoalRepository yamlRepository) {

        List<GPSGoal> stored = sqlRepository.loadAll();

        if (!stored.isEmpty()) return;

        List<GPSGoal> yamlGoals = yamlRepository.loadAll();

        if (yamlGoals.isEmpty()) return;

        sqlRepository.saveAll(yamlGoals);
        plugin.getLogger().info("Imported " + yamlGoals.size() + " mark(s) from goals.yml into SQL.");

    }
}
