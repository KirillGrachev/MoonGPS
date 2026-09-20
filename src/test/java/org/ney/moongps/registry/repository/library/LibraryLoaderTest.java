package org.ney.moongps.registry.repository.library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Лоадер проверяется без сети: url библиотеки указывает на локальный jar
 * из тестовой зависимости через file://.
 */
class LibraryLoaderTest {

    @TempDir
    Path libsFolder;

    @Test
    @DisplayName("Библиотека копируется в libs и драйвер поднимается")
    void copiesAndLoadsDriver() throws Exception {

        LibraryDefinition definition = definition("h2.jar");
        LibraryLoader loader = new LibraryLoader(libsFolder.toFile(), true);

        Driver driver = loader.loadDriver(List.of(definition), "org.h2.Driver");

        assertNotNull(driver);
        assertTrue(Files.exists(libsFolder.resolve("h2.jar")));

    }

    @Test
    @DisplayName("Битый файл в libs перезаписывается заново")
    void corruptedFileRedownloaded() throws Exception {

        Files.write(libsFolder.resolve("h2.jar"), "garbage".getBytes());

        LibraryDefinition definition = definition("h2.jar");
        LibraryLoader loader = new LibraryLoader(libsFolder.toFile(), true);

        loader.loadDriver(List.of(definition), "org.h2.Driver");

        String expected = LibraryLoader.sha256(sourceJar().toFile());

        assertEquals(expected, LibraryLoader.sha256(libsFolder.resolve("h2.jar").toFile()));

    }

    @Test
    @DisplayName("Без файла и с выключенным скачиванием загрузка падает")
    void missingAndDownloadDisabled() throws Exception {

        LibraryDefinition definition = definition("h2.jar");
        LibraryLoader loader = new LibraryLoader(libsFolder.toFile(), false);

        assertThrows(SQLException.class,
                () -> loader.loadDriver(List.of(definition), "org.h2.Driver"));

    }

    private LibraryDefinition definition(String fileName) throws Exception {

        Path source = sourceJar();
        return new LibraryDefinition(fileName, source.toUri().toString(), LibraryLoader.sha256(source.toFile()));

    }

    private Path sourceJar() throws URISyntaxException {
        return Paths.get(org.h2.Driver.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }
}
