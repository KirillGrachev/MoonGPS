package org.ney.moongps.config.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigTypesTest {

    @Test
    @DisplayName("Режим направления разбирается без учёта регистра")
    void directionModeOf() {

        assertEquals(DirectionMode.COMPASS, DirectionMode.of("compass", DirectionMode.RELATIVE));
        assertEquals(DirectionMode.RELATIVE, DirectionMode.of("Relative", DirectionMode.COMPASS));

    }

    @Test
    @DisplayName("Неизвестный режим даёт значение по умолчанию")
    void directionModeFallback() {

        assertEquals(DirectionMode.RELATIVE, DirectionMode.of("nope", DirectionMode.RELATIVE));
        assertEquals(DirectionMode.RELATIVE, DirectionMode.of(null, DirectionMode.RELATIVE));
        assertEquals(DirectionMode.RELATIVE, DirectionMode.of("  ", DirectionMode.RELATIVE));

    }

    @Test
    @DisplayName("Тип хранилища разбирается без учёта регистра")
    void storageTypeOf() {

        assertEquals(StorageType.SQL, StorageType.of("sql", StorageType.YAML));
        assertEquals(StorageType.YAML, StorageType.of("Sqlite", StorageType.YAML));
        assertEquals(StorageType.YAML, StorageType.of(null, StorageType.YAML));

    }

    @Test
    @DisplayName("Прогресс boss bar разбирается без учёта регистра")
    void bossBarProgressOf() {

        assertEquals(BossBarProgress.FULL, BossBarProgress.of("full", BossBarProgress.DISTANCE));
        assertEquals(BossBarProgress.DISTANCE, BossBarProgress.of("Distance", BossBarProgress.FULL));

    }

    @Test
    @DisplayName("Неизвестный прогресс даёт значение по умолчанию")
    void bossBarProgressFallback() {

        assertEquals(BossBarProgress.DISTANCE, BossBarProgress.of("wrong", BossBarProgress.DISTANCE));
        assertEquals(BossBarProgress.DISTANCE, BossBarProgress.of(null, BossBarProgress.DISTANCE));

    }
}
