package org.ney.moongps.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Ресурсы плагина (config.yml, goals.yml, plugin.yml) должны быть на английском:
 * их видит каждый, кто скачал плагин, в том числе на spigotmc.
 */
class ResourcesLanguageTest {

    private static final Pattern CYRILLIC = Pattern.compile("[\\u0400-\\u04FF]");
    private static final List<String> RESOURCES = List.of("config.yml", "goals.yml", "plugin.yml");

    @Test
    @DisplayName("В ресурсах плагина нет кириллицы")
    void resourcesAreEnglish() throws IOException {

        for (String resource : RESOURCES) {

            assertFalse(CYRILLIC.matcher(read(resource)).find(),
                    "В ресурсе " + resource + " найден русский текст");

        }

    }

    private String read(String resource) throws IOException {

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {

            assertNotNull(stream, "Ресурс не найден: " + resource);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        }

    }
}
