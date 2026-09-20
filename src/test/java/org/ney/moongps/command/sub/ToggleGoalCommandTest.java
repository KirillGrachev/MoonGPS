package org.ney.moongps.command.sub;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.command.CommandContext;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.NavigationService;
import org.ney.moongps.util.Placeholders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ToggleGoalCommandTest {

    private ConfigManager configManager;
    private NavigationService navigationService;
    private MessageService messageService;
    private ToggleGoalCommand toggleGoalCommand;
    private Player player;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        navigationService = Mockito.mock(NavigationService.class);
        messageService = Mockito.mock(MessageService.class);

        Mockito.when(configManager.getUsageMessage()).thenReturn(new Messages(List.of("usage"), true));

        toggleGoalCommand = new ToggleGoalCommand(configManager, navigationService, messageService);
        player = Mockito.mock(Player.class);

    }

    @Test
    @DisplayName("Без аргумента и активной метки выводится справка")
    void usageWithoutGoal() {

        toggleGoalCommand.execute(CommandContext.of(player), new String[0]);
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Без аргумента выключает активную метку")
    void stopsActiveGoal() {

        GPSGoal goal = new GPSGoal("shop", 1.0D, 2.0D, 3.0D, "world", null);

        Mockito.when(navigationService.getActiveGoal(player)).thenReturn(goal);

        toggleGoalCommand.execute(CommandContext.of(player), new String[0]);

        Mockito.verify(navigationService).stopNavigation(player, true);

    }

    @Test
    @DisplayName("С аргументом включает навигатор до метки")
    void togglesGoal() {

        toggleGoalCommand.execute(CommandContext.of(player), new String[]{"shop"});
        Mockito.verify(navigationService).toggleGoal(player, "shop", true);

    }

    @Test
    @DisplayName("Подкоманда доступна только игрокам")
    void playerOnly() {
        assertTrue(toggleGoalCommand.isPlayerOnly());
    }
}
