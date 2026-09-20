package org.ney.moongps.service;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.model.Direction;

import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Проверка математики направления.
 * Система координат Minecraft: север - это -Z, восток - +X,
 * а yaw 0 смотрит на юг (Bukkit: getDirection() = (-sin(yaw), cos(yaw))).
 */
class DirectionServiceTest {

    private static final float YAW_SOUTH = 0.0F;
    private static final float YAW_NORTH = 180.0F;
    private static final float YAW_EAST = -90.0F;
    private static final float YAW_WEST = 90.0F;

    @TempDir
    Path dataFolder;

    private World world;
    private DirectionService directionService;

    @BeforeEach
    void setUp() throws IOException {

        try (InputStream configStream = getClass().getClassLoader().getResourceAsStream("direction-config.yml")) {
            Files.copy(configStream, dataFolder.resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING);
        }

        MoonGPS plugin = Mockito.mock(MoonGPS.class);

        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("MoonGPSTest"));

        world = Mockito.mock(World.class);
        directionService = new DirectionService(new ConfigManager(plugin));

    }

    @Test
    @DisplayName("Метка по курсу игрока - AHEAD")
    void goalAhead() {

        assertEquals(Direction.Relative.AHEAD, relative(YAW_NORTH, 0.0D, -10.0D));
        assertEquals(Direction.Relative.AHEAD, relative(YAW_SOUTH, 0.0D, 10.0D));

    }

    @Test
    @DisplayName("Метка за спиной игрока - BEHIND")
    void goalBehind() {

        assertEquals(Direction.Relative.BEHIND, relative(YAW_NORTH, 0.0D, 10.0D));
        assertEquals(Direction.Relative.BEHIND, relative(YAW_SOUTH, 0.0D, -10.0D));

    }

    @Test
    @DisplayName("Метка по правую руку - RIGHT")
    void goalOnTheRight() {

        // Смотрим на север (-Z), восток (+X) справа
        assertEquals(Direction.Relative.RIGHT, relative(YAW_NORTH, 10.0D, 0.0D));
        // Смотрим на юг (+Z), запад (-X) справа
        assertEquals(Direction.Relative.RIGHT, relative(YAW_SOUTH, -10.0D, 0.0D));

    }

    @Test
    @DisplayName("Метка по левую руку - LEFT")
    void goalOnTheLeft() {

        // Смотрим на север (-Z), запад (-X) слева
        assertEquals(Direction.Relative.LEFT, relative(YAW_NORTH, -10.0D, 0.0D));
        // Смотрим на юг (+Z), восток (+X) слева
        assertEquals(Direction.Relative.LEFT, relative(YAW_SOUTH, 10.0D, 0.0D));

    }

    @Test
    @DisplayName("Направление считается от взгляда игрока, а не от севера")
    void directionsDependOnYaw() {

        // Игрок смотрит на восток, метка на севере - слева
        assertEquals(Direction.Relative.LEFT, relative(YAW_EAST, 0.0D, -10.0D));
        // Игрок смотрит на запад, метка на севере - справа
        assertEquals(Direction.Relative.RIGHT, relative(YAW_WEST, 0.0D, -10.0D));

    }

    @Test
    @DisplayName("Диагональ не попадает в узкие секторы прямо/назад")
    void diagonalIsSideDirection() {

        Direction.Relative relative = relative(YAW_NORTH, 10.0D, -10.0D);
        assertEquals(Direction.Relative.RIGHT, relative);

    }

    @Test
    @DisplayName("Стороны света рассчитываются от севера по часовой стрелке")
    void compassDirections() {

        assertEquals(Direction.Compass.NORTH, compassOf(0.0D, -10.0D));
        assertEquals(Direction.Compass.NORTH_EAST, compassOf(10.0D, -10.0D));
        assertEquals(Direction.Compass.EAST, compassOf(10.0D, 0.0D));
        assertEquals(Direction.Compass.SOUTH_EAST, compassOf(10.0D, 10.0D));
        assertEquals(Direction.Compass.SOUTH, compassOf(0.0D, 10.0D));
        assertEquals(Direction.Compass.SOUTH_WEST, compassOf(-10.0D, 10.0D));
        assertEquals(Direction.Compass.WEST, compassOf(-10.0D, 0.0D));
        assertEquals(Direction.Compass.NORTH_WEST, compassOf(-10.0D, -10.0D));

    }

    @Test
    @DisplayName("Расстояние считается в блоках")
    void distanceCalculated() {

        double distance = directionService.calculateDistance(at(0.0D, 0.0D, 0.0D), at(3.0D, 0.0D, 4.0D));
        assertEquals(5.0D, distance, 0.0001D);

    }

    @Test
    @DisplayName("Формат направления подставляет стрелку и название")
    void formatAppliesPattern() {

        Direction direction = directionService.calculateDirection(player(YAW_NORTH), goal(0.0D, -10.0D));
        assertEquals("[↑] AHEAD", directionService.format(direction));

    }

    @Test
    @DisplayName("COMPASS оформляет сторону света названием из конфига")
    void compassFormat() {

        DirectionService compassService = config("COMPASS", "{arrow} {name}");
        Direction direction = compassService.calculateDirection(player(YAW_SOUTH), goal(0.0D, -10.0D));

        assertEquals("↑ N", compassService.format(direction));

    }

    @Test
    @DisplayName("Отсутствующее название направления заменяется стрелкой")
    void missingNameFallsBackToArrow() {

        DirectionService sparseService = config("RELATIVE", "{arrow} {name}");
        Direction direction = sparseService.calculateDirection(player(YAW_NORTH), goal(10.0D, 0.0D));

        assertEquals("→ →", sparseService.format(direction));

    }

    @Test
    @DisplayName("Формат без плейсхолдеров возвращает название направления")
    void formatWithoutPlaceholders() {

        DirectionService plainService = config("RELATIVE", "plain text");
        Direction direction = plainService.calculateDirection(player(YAW_NORTH), goal(0.0D, -10.0D));

        assertEquals("AHEAD", plainService.format(direction));

    }

    private DirectionService config(String mode, String format) {

        try {

            Path folder = Files.createTempDirectory("moongps-direction");
            Files.writeString(folder.resolve("config.yml"), ""
                    + "settings:\n"
                    + "  direction:\n"
                    + "    mode: " + mode + "\n"
                    + "    format: \"" + format + "\"\n"
                    + "    relative:\n"
                    + "      AHEAD: \"AHEAD\"\n"
                    + "    compass:\n"
                    + "      NORTH: \"N\"\n"
                    + "messages:\n"
                    + "  prefix: \"\"\n");

            MoonGPS plugin = Mockito.mock(MoonGPS.class);

            Mockito.when(plugin.getDataFolder()).thenReturn(folder.toFile());
            Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("MoonGPSTest"));

            return new DirectionService(new ConfigManager(plugin));

        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }

    }

    private Direction.Relative relative(float yaw, double goalX, double goalZ) {
        return directionService.calculateDirection(player(yaw), goal(goalX, goalZ)).relative();
    }

    private Direction.Compass compassOf(double deltaX, double deltaZ) {
        return directionService.calculateDirection(player(YAW_SOUTH), goal(deltaX, deltaZ)).compass();
    }

    private Location player(float yaw) {
        return new Location(world, 0.0D, 64.0D, 0.0D, yaw, 0.0F);
    }

    private Location goal(double x, double z) {
        return at(x, 64.0D, z);
    }

    private Location at(double x, double y, double z) {
        return new Location(world, x, y, z);
    }
}
