package org.ney.moongps.command.sub;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.command.CommandContext;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.registry.GoalRegistry;
import org.ney.moongps.registry.GoalStorage;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.NavigationService;
import org.ney.moongps.util.Placeholders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteGoalCommandTest {

    private static final GPSGoal GOAL = new GPSGoal("shop", 1.0D, 2.0D, 3.0D, "lobby", null);

    private ConfigManager configManager;
    private GoalRegistry goalRegistry;
    private GoalStorage goalStorage;
    private NavigationService navigationService;
    private MessageService messageService;
    private DeleteGoalCommand deleteGoalCommand;
    private CommandSender sender;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        goalRegistry = Mockito.mock(GoalRegistry.class);
        goalStorage = Mockito.mock(GoalStorage.class);
        navigationService = Mockito.mock(NavigationService.class);
        messageService = Mockito.mock(MessageService.class);

        Mockito.when(configManager.getUsageMessage()).thenReturn(new Messages(List.of("usage"), true));

        deleteGoalCommand = new DeleteGoalCommand(configManager, goalRegistry, goalStorage, navigationService, messageService);
        sender = Mockito.mock(CommandSender.class);

    }

    @Test
    @DisplayName("Без названия выводится справка")
    void usageWithoutName() {

        deleteGoalCommand.execute(CommandContext.of(sender), new String[0]);
        Mockito.verify(messageService).send(Mockito.eq(sender), Mockito.any(Messages.class), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Несуществующая метка даёт ошибку")
    void goalNotFound() {

        Messages notFound = new Messages(List.of("not found"), true);

        Mockito.when(configManager.getMarkNotFoundMessage()).thenReturn(notFound);

        deleteGoalCommand.execute(CommandContext.of(sender), new String[]{"shop"});

        Mockito.verify(messageService).send(Mockito.eq(sender), Mockito.eq(notFound), Mockito.any(Placeholders.class));
        Mockito.verify(goalStorage, Mockito.never()).deleteGoal(Mockito.any());

    }

    @Test
    @DisplayName("Удаление снимает метку из реестра, файла и сессий")
    void deletesGoal() {

        Mockito.when(goalRegistry.getGoal("shop")).thenReturn(GOAL);
        Mockito.when(goalRegistry.removeGoal("shop")).thenReturn(GOAL);

        Messages success = new Messages(List.of("deleted"), true);

        Mockito.when(configManager.getDeleteSuccessMessage()).thenReturn(success);

        deleteGoalCommand.execute(CommandContext.of(sender), new String[]{"shop"});

        Mockito.verify(navigationService).stopGoalForEveryone("shop");
        Mockito.verify(goalRegistry).removeGoal("shop");
        Mockito.verify(goalStorage).deleteGoal("shop");
        Mockito.verify(messageService).send(Mockito.eq(sender), Mockito.eq(success), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Автодополнение предлагает названия меток")
    void completeNames() {

        Mockito.when(goalRegistry.getSortedGoalNames()).thenReturn(List.of("shop", "bank"));
        assertEquals(List.of("shop", "bank"), deleteGoalCommand.complete(CommandContext.of(sender), new String[]{""}));
        assertEquals(List.of(), deleteGoalCommand.complete(CommandContext.of(sender), new String[]{"shop", ""}));

    }
}
