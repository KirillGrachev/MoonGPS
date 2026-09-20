package org.ney.moongps.command.sub;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
import org.ney.moongps.support.BukkitSupport;

import java.util.List;
import org.ney.moongps.util.Placeholders;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetGoalCommandTest {

    private ConfigManager configManager;
    private GoalRegistry goalRegistry;
    private GoalStorage goalStorage;
    private NavigationService navigationService;
    private MessageService messageService;
    private SetGoalCommand setGoalCommand;
    private Player player;
    private World world;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        goalRegistry = Mockito.mock(GoalRegistry.class);
        goalStorage = Mockito.mock(GoalStorage.class);
        navigationService = Mockito.mock(NavigationService.class);
        messageService = Mockito.mock(MessageService.class);

        Mockito.when(configManager.getUsageMessage()).thenReturn(new Messages(List.of("usage"), true));

        setGoalCommand = new SetGoalCommand(configManager, goalRegistry, goalStorage, navigationService, messageService);

        player = Mockito.mock(Player.class);
        world = BukkitSupport.world("world");

        Mockito.when(player.getLocation()).thenReturn(new Location(world, 10.2D, 70.0D, 10.7D));

    }

    @Test
    @DisplayName("Без названия выводится справка")
    void usageWithoutName() {

        setGoalCommand.execute(CommandContext.of(player), new String[0]);
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Некорректное название отклоняется")
    void invalidName() {

        Messages invalidName = new Messages(List.of("invalid"), true);

        Mockito.when(configManager.getSetInvalidNameMessage()).thenReturn(invalidName);

        setGoalCommand.execute(CommandContext.of(player), new String[]{"my mark"});

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(invalidName), Mockito.any(Placeholders.class));
        Mockito.verify(goalRegistry, Mockito.never()).registerGoal(Mockito.any());

    }

    @Test
    @DisplayName("Новая метка создаётся по позиции игрока и сохраняется")
    void createsGoal() {

        Messages success = new Messages(List.of("created"), true);

        Mockito.when(configManager.getSetSuccessMessage()).thenReturn(success);

        setGoalCommand.execute(CommandContext.of(player), new String[]{"shop"});

        org.mockito.ArgumentCaptor<GPSGoal> captor = org.mockito.ArgumentCaptor.forClass(GPSGoal.class);

        Mockito.verify(goalRegistry).registerGoal(captor.capture());
        Mockito.verify(goalStorage).saveGoal(captor.getValue());
        Mockito.verify(navigationService).refreshGoalForEveryone("shop");
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(success), Mockito.any(Placeholders.class));

        GPSGoal goal = captor.getValue();

        assertEquals("shop", goal.name());
        assertEquals(10.5D, goal.x());
        assertEquals(10.5D, goal.z());
        assertEquals("world", goal.world());

    }

    @Test
    @DisplayName("Существующая метка обновляется сообщением updated")
    void updatesGoal() {

        Messages updated = new Messages(List.of("updated"), true);

        Mockito.when(goalRegistry.isGoalRegistered("shop")).thenReturn(true);
        Mockito.when(configManager.getSetUpdatedMessage()).thenReturn(updated);

        setGoalCommand.execute(CommandContext.of(player), new String[]{"shop"});

        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(updated), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Автодополнение предлагает обновляемые метки и используемые права")
    void completeArguments() {

        Mockito.when(goalRegistry.getSortedGoalNames()).thenReturn(List.of("shop"));
        Mockito.when(goalRegistry.getUsedPermissions()).thenReturn(List.of("moongps.mark.bank"));

        assertEquals(List.of("shop"), setGoalCommand.complete(CommandContext.of(player), new String[]{""}));
        assertEquals(List.of("moongps.mark.bank"), setGoalCommand.complete(CommandContext.of(player), new String[]{"shop", ""}));
        assertEquals(List.of(), setGoalCommand.complete(CommandContext.of(player), new String[]{"shop", "perm", ""}));

    }

    @Test
    @DisplayName("Второй аргумент становится правом метки")
    void goalPermission() {

        setGoalCommand.execute(CommandContext.of(player), new String[]{"shop", "moongps.mark.shop"});

        org.mockito.ArgumentCaptor<GPSGoal> captor = org.mockito.ArgumentCaptor.forClass(GPSGoal.class);

        Mockito.verify(goalRegistry).registerGoal(captor.capture());

        assertEquals("moongps.mark.shop", captor.getValue().permission());

    }
}
