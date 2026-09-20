package org.ney.moongps.registry.repository;

import org.jetbrains.annotations.NotNull;
import org.ney.moongps.config.type.SqlSettings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.StringJoiner;

/**
 * Подключения к MySQL через драйвер JDBC, вшитый в jar плагина.
 */
public class MysqlConnectionFactory implements ConnectionFactory {

    private final SqlSettings settings;

    public MysqlConnectionFactory(@NotNull SqlSettings settings) {
        this.settings = settings;
    }

    @Override
    public Connection open() throws SQLException {
        return DriverManager.getConnection(buildUrl(), settings.user(), settings.password());
    }

    /**
     * Собирает JDBC-URL из настроек и дополнительных параметров.
     *
     * @return URL вида jdbc:mysql://host:port/database?params
     */
    private @NotNull String buildUrl() {

        StringJoiner params = new StringJoiner("&");

        settings.properties().forEach((key, value) -> params.add(key + "=" + value));

        String query = params.length() == 0 ? "" : "?" + params;

        return "jdbc:mysql://" + settings.host() + ":" + settings.port() + "/" + settings.database() + query;

    }
}
