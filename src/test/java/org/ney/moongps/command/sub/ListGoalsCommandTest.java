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
import org.ney.moongps.service.GoalVisibilityService;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.support.BukkitSupport;
import org.ney.moongps.util.Placeholders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListGoalsCommandTest {

    private ConfigManager configManager;
    private MessageService messageService;
    private ListGoalsCommand listGoalsCommand;
    private GoalVisibilityService goalVisibilityService;
    private Player player;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        messageService = Mockito.mock(MessageService.class);
        player = Mockito.mock(Player.class);

        goalVisibilityService = Mockito.mock(GoalVisibilityService.class);

        Mockito.when(goalVisibilityService.filterVisible(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> List.copyOf(invocation.getArgument(1, java.util.Collection.class)));

        Mockito.when(configManager.getListEmptyMessage()).thenReturn(new Messages(List.of("empty"), true));

        listGoalsCommand = new ListGoalsCommand(configManager, messageService, List::of, goalVisibilityService);

    }

    @Test
    @DisplayName("Пустой реестр даёт сообщение о пустом списке")
    void emptyRegistry() {

        listGoalsCommand.execute(CommandContext.of(player), new String[0]);
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.any(Messages.class), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Метки выводятся списком с расстоянием и пагинацией")
    void entriesRendered() {

        org.ney.moongps.model.GPSGoal shop = new org.ney.moongps.model.GPSGoal("shop", 3.0D, 70.0D, 4.0D, "world", null);
        org.ney.moongps.model.GPSGoal far = new org.ney.moongps.model.GPSGoal("mine", 0.0D, 70.0D, 0.0D, "other", null);

        World lobby = BukkitSupport.world("world");
        BukkitSupport.world("other");

        listGoalsCommand = new ListGoalsCommand(configManager, messageService, () -> List.of(shop, far), goalVisibilityService);

        Mockito.when(configManager.getListPerPage()).thenReturn(10);
        Mockito.when(configManager.getListHeader()).thenReturn(new Messages(List.of("§bMarks {page}/{max_page}"), true));
        Mockito.when(configManager.getListFooter()).thenReturn(new Messages(List.of("§7Total: {total}"), true));
        Mockito.when(configManager.getListEntryFormat()).thenReturn(new Messages(List.of("§f{gps} {distance}"), true));
        Mockito.when(configManager.getListOtherWorldEntryFormat()).thenReturn(new Messages(List.of("§f{gps} {world}"), true));

        Mockito.when(player.getLocation()).thenReturn(new Location(lobby, 0.0D, 70.0D, 0.0D));

        org.mockito.ArgumentCaptor<String> captor = org.mockito.ArgumentCaptor.forClass(String.class);

        listGoalsCommand.execute(CommandContext.of(player), new String[0]);

        Mockito.verify(player, Mockito.times(4)).sendMessage(captor.capture());

        List<String> lines = captor.getAllValues();

        assertEquals("§bMarks 1/1", lines.get(0));
        assertEquals("§fmine other", lines.get(1));
        assertEquals("§fshop 5", lines.get(2));
        assertEquals("§7Total: 2", lines.get(3));

    }

    @Test
    @DisplayName("Номер страницы ограничивается последней страницей")
    void pageClamped() {

        listGoalsCommand = new ListGoalsCommand(configManager, messageService, () -> List.of(
                new org.ney.moongps.model.GPSGoal("a", 0.0D, 0.0D, 0.0D, "world", null)
        ), goalVisibilityService);

        Mockito.when(configManager.getListPerPage()).thenReturn(1);
        Mockito.when(configManager.getListHeader()).thenReturn(new Messages(List.of("page {page}"), true));
        Mockito.when(configManager.getListFooter()).thenReturn(Messages.disabled());
        Mockito.when(configManager.getListEntryFormat()).thenReturn(new Messages(List.of("{gps}"), true));

        World lobby = BukkitSupport.world("world");

        Mockito.when(player.getLocation()).thenReturn(new Location(lobby, 0.0D, 0.0D, 0.0D));

        org.mockito.ArgumentCaptor<String> captor = org.mockito.ArgumentCaptor.forClass(String.class);

        listGoalsCommand.execute(CommandContext.of(player), new String[]{"99"});

        Mockito.verify(player, Mockito.times(2)).sendMessage(captor.capture());

        assertEquals("page 1", captor.getAllValues().get(0));

    }

    @Test
    @DisplayName("Автодополнение предлагает номера страниц")
    void completePages() {

        listGoalsCommand = new ListGoalsCommand(configManager, messageService, () -> List.of(
                new org.ney.moongps.model.GPSGoal("a", 0.0D, 0.0D, 0.0D, "world", null),
                new org.ney.moongps.model.GPSGoal("b", 0.0D, 0.0D, 0.0D, "world", null)
        ), goalVisibilityService);

        Mockito.when(configManager.getListPerPage()).thenReturn(1);

        assertEquals(List.of("1", "2"), listGoalsCommand.complete(CommandContext.of(player), new String[]{""}));
        assertEquals(List.of(), listGoalsCommand.complete(CommandContext.of(player), new String[]{"1", ""}));

    }
}
