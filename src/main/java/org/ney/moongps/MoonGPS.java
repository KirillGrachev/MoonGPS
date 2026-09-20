package org.ney.moongps;

import org.bukkit.plugin.java.JavaPlugin;
import org.ney.moongps.command.CommandDispatcher;
import org.ney.moongps.command.GpsCommand;
import org.ney.moongps.command.sub.DeleteGoalCommand;
import org.ney.moongps.command.sub.ListGoalsCommand;
import org.ney.moongps.command.sub.ReloadCommand;
import org.ney.moongps.command.sub.SetGoalCommand;
import org.ney.moongps.command.sub.ToggleGoalCommand;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.event.EventDispatcher;
import org.ney.moongps.listener.PlayerJoinListener;
import org.ney.moongps.listener.PlayerQuitListener;
import org.ney.moongps.registry.GoalRegistry;
import org.ney.moongps.registry.GoalStorage;
import org.ney.moongps.registry.repository.GoalRepositoryFactory;
import org.ney.moongps.service.BossBarService;
import org.ney.moongps.service.DirectionService;
import org.ney.moongps.service.GoalNotifier;
import org.ney.moongps.service.GoalVisibilityService;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.NavigationService;
import org.ney.moongps.service.NavigationTaskService;
import org.ney.moongps.service.PermissionService;

import java.util.List;

// Класс не final: MockBukkit в тестах поднимает его подклассом
public class MoonGPS extends JavaPlugin {

    private ConfigManager configManager;
    private GoalRegistry goalRegistry;
    private GoalStorage goalStorage;
    private NavigationService navigationService;

    @Override
    public void onEnable() {

        this.configManager = new ConfigManager(this);
        this.goalRegistry = new GoalRegistry(configManager);
        this.goalStorage = new GoalStorage(
                goalRegistry,
                new GoalRepositoryFactory(this, configManager).create()
        );

        MessageService messageService = new MessageService(configManager);
        PermissionService permissionService = new PermissionService(configManager);
        GoalVisibilityService goalVisibilityService = new GoalVisibilityService(configManager, permissionService);

        goalStorage.loadGoals();

        if (!configManager.isNavigatorEnabled()) {
            getLogger().warning("Navigator is disabled in config.yml (settings.enabled: false)");
        }

        DirectionService directionService = new DirectionService(configManager);
        BossBarService bossBarService = new BossBarService(this, configManager);
        GoalNotifier goalNotifier = new GoalNotifier(configManager, messageService, directionService, bossBarService);

        NavigationTaskService navigationTaskService = new NavigationTaskService(
                this, configManager, goalRegistry,
                directionService, goalNotifier, messageService
        );

        this.navigationService = new NavigationService(
                configManager, goalRegistry, navigationTaskService, messageService,
                permissionService, goalVisibilityService, bossBarService, goalNotifier
        );

        ToggleGoalCommand toggleGoalCommand =
                new ToggleGoalCommand(configManager, navigationService, messageService);

        // Регистрация команд
        new CommandDispatcher(this).registerCommand("gps", new GpsCommand(
                configManager,
                messageService,
                permissionService,
                toggleGoalCommand,
                List.of(
                        toggleGoalCommand,
                        new ListGoalsCommand(configManager, messageService, goalRegistry::getSortedGoals, goalVisibilityService),
                        new SetGoalCommand(configManager, goalRegistry, goalStorage, navigationService, messageService),
                        new DeleteGoalCommand(configManager, goalRegistry, goalStorage, navigationService, messageService),
                        new ReloadCommand(this, configManager, messageService)
                ),
                goalRegistry::getSortedGoals,
                goalVisibilityService
        ));

        // Регистрация слушателей
        new EventDispatcher(this).registerEvents(
                new PlayerQuitListener(navigationService),
                new PlayerJoinListener(configManager, navigationService)
        );

        if (configManager.isNavigatorEnabled()) {
            startAutoNavigation();
        }

        getLogger().info("MoonGPS enabled! Marks loaded: " + goalRegistry.size());

    }

    @Override
    public void onDisable() {

        if (goalStorage != null) {

            goalStorage.saveGoals();
            goalStorage.close();

        }

        if (navigationService != null) {
            navigationService.cancelAll();
        }

        getLogger().info("MoonGPS disabled!");

    }

    /**
     * Перезагружает конфигурацию, метки и активные сессии навигации.
     *
     * @return количество загруженных меток
     */
    public int reloadPlugin() {

        configManager.reload();

        if (configManager.getStorageSettings().type() != goalStorage.getStorageType()) {
            getLogger().warning("Storage type changed in config.yml: restart the server to switch storage.");
        } else {
            goalStorage.loadGoals();
        }

        if (navigationService != null) {
            navigationService.stopAll(true);
        }

        return goalRegistry.size();

    }

    private void startAutoNavigation() {

        String autoStartGoal = configManager.getAutoStartGoal();
        if (autoStartGoal.isEmpty()) return;

        getServer().getOnlinePlayers().forEach(player ->
                navigationService.toggleGoal(player, autoStartGoal, false)
        );

    }

    public GoalRegistry getGoalRegistry() {
        return goalRegistry;
    }

    public NavigationService getNavigationService() {
        return navigationService;
    }
}
