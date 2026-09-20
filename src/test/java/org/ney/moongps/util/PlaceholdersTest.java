package org.ney.moongps.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholdersTest {

    @Test
    @DisplayName("Плейсхолдеры подставляются в текст")
    void placeholdersApplied() {

        String result = Placeholders.create()
                .add("gps", "shop")
                .add("distance", 42)
                .apply("Метка {gps} - {distance} бл.");
        assertEquals("Метка shop - 42 бл.", result);

    }

    @Test
    @DisplayName("Одинаковый плейсхолдер заменяется во всех вхождениях")
    void placeholderReplacedEverywhere() {

        String result = Placeholders.create()
                .add("gps", "shop")
                .apply("{gps} и ещё {gps}");
        assertEquals("shop и ещё shop", result);

    }

    @Test
    @DisplayName("Неизвестный плейсхолдер остаётся в тексте")
    void unknownPlaceholderKept() {

        String result = Placeholders.create()
                .add("gps", "shop")
                .apply("{gps} {unknown}");
        assertEquals("shop {unknown}", result);

    }
}
