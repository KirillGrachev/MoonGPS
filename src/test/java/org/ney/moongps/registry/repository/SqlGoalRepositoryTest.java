package org.ney.moongps.registry.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ney.moongps.config.type.SqlSettings;
import org.ney.moongps.model.GPSGoal;

import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Репозиторий прогоняется на H2 в режиме совместимости с MySQL:
 * те же запросы, та же логика upsert и транзакций.
 */
class SqlGoalRepositoryTest {

    private SqlGoalRepository sqlGoalRepository;

    @BeforeEach
    void setUp() throws Exception {

        SqlSettings settings = new SqlSettings(
                "localhost", 3306, "moongps", "test_marks", "root", "", Map.of()
        );

        sqlGoalRepository = new SqlGoalRepository(database(settings), settings, Logger.getLogger("MoonGPSTest"));
        sqlGoalRepository.connect();

    }

    @AfterEach
    void tearDown() {
        sqlGoalRepository.close();
    }

    @Test
    @DisplayName("Метка сохраняется и читается обратно")
    void saveAndLoad() {

        sqlGoalRepository.save(new GPSGoal("shop", 1.5D, 2.5D, 3.5D, "world", null));

        List<GPSGoal> goals = sqlGoalRepository.loadAll();

        assertEquals(1, goals.size());
        assertEquals("shop", goals.get(0).name());
        assertEquals(2.5D, goals.get(0).y());
        assertEquals("world", goals.get(0).world());

    }

    @Test
    @DisplayName("Повторное сохранение обновляет метку, а не дублирует")
    void saveUpdates() {

        sqlGoalRepository.save(new GPSGoal("shop", 1.0D, 1.0D, 1.0D, "world", null));
        sqlGoalRepository.save(new GPSGoal("shop", 9.0D, 9.0D, 9.0D, "world", "moongps.mark.shop"));

        List<GPSGoal> goals = sqlGoalRepository.loadAll();

        assertEquals(1, goals.size());
        assertEquals(9.0D, goals.get(0).x());
        assertEquals("moongps.mark.shop", goals.get(0).permission());

    }

    @Test
    @DisplayName("saveAll пишет пачкой")
    void saveAllBatch() {

        sqlGoalRepository.saveAll(List.of(
                new GPSGoal("bank", 1.0D, 1.0D, 1.0D, "world", null),
                new GPSGoal("shop", 2.0D, 2.0D, 2.0D, "world", null)
        ));
        assertEquals(2, sqlGoalRepository.loadAll().size());

    }

    @Test
    @DisplayName("Удаление убирает метку из таблицы")
    void deleteRemoves() {

        sqlGoalRepository.save(new GPSGoal("shop", 1.0D, 1.0D, 1.0D, "world", null));
        sqlGoalRepository.delete("shop");

        assertTrue(sqlGoalRepository.loadAll().isEmpty());

    }

    @Test
    @DisplayName("После close подключение поднимается заново")
    void reconnectAfterClose() {

        sqlGoalRepository.save(new GPSGoal("shop", 1.0D, 1.0D, 1.0D, "world", null));

        sqlGoalRepository.close();

        assertEquals(1, sqlGoalRepository.loadAll().size());

    }

    @Test
    @DisplayName("Небезопасное имя таблицы заменяется на стандартное")
    void unsafeTableNameFallsBack() throws Exception {

        SqlSettings settings = new SqlSettings(
                "localhost", 3306, "moongps", "bad name; drop", "root", "", Map.of()
        );

        SqlGoalRepository unsafeRepository = new SqlGoalRepository(
                database(settings), settings, Logger.getLogger("MoonGPSTest")
        );

        unsafeRepository.connect();
        unsafeRepository.save(new GPSGoal("shop", 1.0D, 1.0D, 1.0D, "world", null));

        assertEquals(1, unsafeRepository.loadAll().size());

        unsafeRepository.close();

    }

    private ConnectionFactory database(SqlSettings settings) {

        String url = "jdbc:h2:mem:moongps_" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        return () -> DriverManager.getConnection(url);

    }
}
