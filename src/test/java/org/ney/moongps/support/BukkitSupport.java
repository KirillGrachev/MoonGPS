package org.ney.moongps.support;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

/**
 * Тестовая опора: один раз подставляет mock-Server в Bukkit,
 * чтобы статические вызовы Bukkit.getPluginManager() и Bukkit.getWorld()
 * работали без поднятия настоящего сервера.
 */
public final class BukkitSupport {

    private static final Server SERVER = Mockito.mock(Server.class);

    static {

        // Bukkit.setServer() сам пишет лог через server.getLogger()
        Mockito.when(SERVER.getLogger()).thenReturn(java.util.logging.Logger.getLogger("MoonGPS-Test"));
        Bukkit.setServer(SERVER);

    }

    private BukkitSupport() {

    }

    public static @NotNull Server server() {
        return SERVER;
    }

    /**
     * Устанавливает свежий plugin manager на текущий тест.
     *
     * @return mock plugin manager
     */
    public static @NotNull PluginManager newPluginManager() {

        PluginManager pluginManager = Mockito.mock(PluginManager.class);

        Mockito.when(SERVER.getPluginManager()).thenReturn(pluginManager);

        return pluginManager;

    }

    /**
     * "Загружает" мир на mock-сервере.
     *
     * @param name название мира
     * @return mock мира
     */
    public static @NotNull World world(@NotNull String name) {

        World world = Mockito.mock(World.class);

        Mockito.when(world.getName()).thenReturn(name);
        Mockito.when(SERVER.getWorld(name)).thenReturn(world);

        return world;

    }

    /**
     * Выгружает все миры: Bukkit.getWorld() снова возвращает null.
     */
    public static void clearWorlds() {
        Mockito.when(SERVER.getWorld(Mockito.anyString())).thenReturn(null);
    }

    /**
     * Регистрирует игрока на mock-сервере по UUID.
     *
     * @param player mock игрока
     */
    public static void onlinePlayer(@NotNull org.bukkit.entity.Player player) {
        Mockito.when(SERVER.getPlayer(player.getUniqueId())).thenReturn(player);
    }
}
