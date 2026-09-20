package org.ney.moongps.registry.repository;

import org.jetbrains.annotations.NotNull;
import org.ney.moongps.config.type.SqlSettings;
import org.ney.moongps.model.GPSGoal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Хранилище меток в таблице MySQL.
 * Подключение открывается лениво и переподнимается при обрыве.
 */
public class SqlGoalRepository implements GoalRepository {

    private static final String TABLE_PATTERN = "[A-Za-z0-9_]+";

    private final ConnectionFactory connectionFactory;
    private final SqlSettings settings;
    private final Logger logger;

    private Connection connection;

    public SqlGoalRepository(@NotNull ConnectionFactory connectionFactory,
                             @NotNull SqlSettings settings,
                             @NotNull Logger logger) {

        this.connectionFactory = connectionFactory;
        this.settings = settings;
        this.logger = logger;

    }

    /**
     * Поднимает подключение и создаёт таблицу, если её ещё нет.
     *
     * @throws SQLException если база недоступна
     */
    public void connect() throws SQLException {

        Connection current = connection();
        try (Statement statement = current.createStatement()) {
            statement.executeUpdate(createTableQuery());
        }

    }

    @Override
    public @NotNull List<GPSGoal> loadAll() {

        List<GPSGoal> goals = new ArrayList<>();

        try {

            String query = "SELECT name, x, y, z, world, permission FROM " + table() + "";
            try (PreparedStatement statement = connection().prepareStatement(query);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String permission = result.getString("permission");
                    goals.add(new GPSGoal(
                            result.getString("name"),
                            result.getDouble("x"),
                            result.getDouble("y"),
                            result.getDouble("z"),
                            result.getString("world"),
                            permission == null || permission.isBlank() ? null : permission
                    ));

                }

            }

        } catch (SQLException exception) {
            logger.severe("Failed to load marks from SQL: " + exception.getMessage());
        }

        return goals;

    }

    @Override
    public void save(@NotNull GPSGoal goal) {

        String query = "INSERT INTO " + table() + " (name, x, y, z, world, permission) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE x = VALUES(x), y = VALUES(y), z = VALUES(z), "
                + "world = VALUES(world), permission = VALUES(permission)";
        try (PreparedStatement statement = connection().prepareStatement(query)) {

            fillStatement(statement, goal);
            statement.executeUpdate();

        } catch (SQLException exception) {
            logger.severe("Failed to save mark '" + goal.name() + "' to SQL: " + exception.getMessage());
        }

    }

    @Override
    public void saveAll(@NotNull List<GPSGoal> goals) {

        String query = "INSERT INTO " + table() + " (name, x, y, z, world, permission) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE x = VALUES(x), y = VALUES(y), z = VALUES(z), "
                + "world = VALUES(world), permission = VALUES(permission)";
        try {

            Connection current = connection();
            boolean autoCommit = current.getAutoCommit();

            current.setAutoCommit(false);

            try (PreparedStatement statement = current.prepareStatement(query)) {

                for (GPSGoal goal : goals) {

                    fillStatement(statement, goal);
                    statement.addBatch();

                }

                statement.executeBatch();
                current.commit();

            } catch (SQLException exception) {

                current.rollback();
                throw exception;

            } finally {
                current.setAutoCommit(autoCommit);
            }

        } catch (SQLException exception) {
            logger.severe("Failed to save marks to SQL: " + exception.getMessage());
        }

    }

    @Override
    public void delete(@NotNull String goalName) {

        String query = "DELETE FROM " + table() + " WHERE name = ?";
        try (PreparedStatement statement = connection().prepareStatement(query)) {

            statement.setString(1, goalName);
            statement.executeUpdate();

        } catch (SQLException exception) {
            logger.severe("Failed to delete mark '" + goalName + "' from SQL: " + exception.getMessage());
        }

    }

    @Override
    public void close() {

        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException exception) {
            logger.warning("Failed to close SQL connection: " + exception.getMessage());
        } finally {
            connection = null;
        }

    }

    /* Внутренняя логика подключения */

    private @NotNull Connection connection() throws SQLException {

        if (connection != null && connection.isValid(2)) {
            return connection;
        }

        connection = connectionFactory.open();

        return connection;

    }

    private void fillStatement(@NotNull PreparedStatement statement, @NotNull GPSGoal goal) throws SQLException {

        statement.setString(1, goal.name());
        statement.setDouble(2, goal.x());
        statement.setDouble(3, goal.y());
        statement.setDouble(4, goal.z());
        statement.setString(5, goal.world());
        statement.setString(6, goal.permission());

    }

    private @NotNull String createTableQuery() {

        return "CREATE TABLE IF NOT EXISTS " + table() + " ("
                + "name VARCHAR(64) NOT NULL PRIMARY KEY, "
                + "x DOUBLE NOT NULL, "
                + "y DOUBLE NOT NULL, "
                + "z DOUBLE NOT NULL, "
                + "world VARCHAR(64) NOT NULL, "
                + "permission VARCHAR(128) NULL"
                + ")";

    }

    /**
     * Возвращает безопасное название таблицы.
     *
     * @return название таблицы из конфига или значение по умолчанию
     */
    private @NotNull String table() {

        String table = settings.table();

        if (table == null || !table.matches(TABLE_PATTERN)) {
            return "moongps_marks";
        }

        return table;

    }
}
