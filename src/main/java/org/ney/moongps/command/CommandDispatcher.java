package org.ney.moongps.command;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.MoonGPS;

/**
 * Диспетчер регистрации команд плагина.
 */
public class CommandDispatcher {

    private final MoonGPS plugin;

    public CommandDispatcher(@NotNull MoonGPS plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует обработчик и автодополнение команды.
     *
     * @param commandName название команды из plugin.yml
     * @param tabExecutor обработчик команды
     */
    public void registerCommand(@NotNull String commandName, @NotNull TabExecutor tabExecutor) {

        PluginCommand pluginCommand = plugin.getCommand(commandName);

        if (pluginCommand == null) {

            plugin.getLogger().severe("Command '" + commandName + "' is not declared in plugin.yml");
            return;

        }

        pluginCommand.setExecutor(tabExecutor);
        pluginCommand.setTabCompleter(tabExecutor);

    }
}
