package org.ney.moongps.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.service.GoalVisibilityService;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.PermissionService;
import org.ney.moongps.util.Placeholders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GpsCommandTest {

    private ConfigManager configManager;
    private MessageService messageService;
    private PermissionService permissionService;
    private FakeSubCommand defaultSubCommand;
    private FakeSubCommand listSubCommand;
    private GoalVisibilityService goalVisibilityService;
    private GpsCommand gpsCommand;
    private Command command;
    private Player player;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        messageService = Mockito.mock(MessageService.class);
        permissionService = Mockito.mock(PermissionService.class);

        defaultSubCommand = new FakeSubCommand("", null, true, true);
        listSubCommand = new FakeSubCommand("list", null, true, false);

        goalVisibilityService = Mockito.mock(GoalVisibilityService.class);

        Mockito.when(goalVisibilityService.filterVisible(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> List.copyOf(invocation.getArgument(1, java.util.Collection.class)));

        gpsCommand = new GpsCommand(
                configManager, messageService, permissionService,
                defaultSubCommand, List.of(defaultSubCommand, listSubCommand),
                () -> List.of(goal("shop"), goal("bank")),
                goalVisibilityService
        );

        command = Mockito.mock(Command.class);
        Mockito.when(command.getName()).thenReturn("gps");

        player = Mockito.mock(Player.class);

        Mockito.when(permissionService.hasPermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(configManager.getPermissionUse()).thenReturn("moongps.use");

    }

    @Test
    @DisplayName("Чужая команда не обрабатывается")
    void foreignCommand() {

        Command other = Mockito.mock(Command.class);
        Mockito.when(other.getName()).thenReturn("tpa");

        assertFalse(gpsCommand.onCommand(player, other, "tpa", new String[0]));

    }

    @Test
    @DisplayName("Без аргументов вызывается подкоманда по умолчанию")
    void noArguments() {

        gpsCommand.onCommand(player, command, "gps", new String[0]);

        assertEquals(1, defaultSubCommand.calls.size());
        assertEquals(0, defaultSubCommand.calls.get(0).length);

    }

    @Test
    @DisplayName("Известная подкоманда получает свои аргументы")
    void knownSubCommand() {

        gpsCommand.onCommand(player, command, "gps", new String[]{"list", "2"});

        assertEquals(1, listSubCommand.calls.size());
        assertEquals("2", listSubCommand.calls.get(0)[0]);

    }

    @Test
    @DisplayName("Неизвестный первый аргумент считается меткой")
    void unknownArgumentGoesToDefault() {

        gpsCommand.onCommand(player, command, "gps", new String[]{"shop"});
        assertEquals("shop", defaultSubCommand.calls.get(0)[0]);

    }

    @Test
    @DisplayName("Консоль не выполняет player-only подкоманды")
    void consoleBlocked() {

        CommandSender console = Mockito.mock(CommandSender.class);

        gpsCommand.onCommand(console, command, "gps", new String[]{"list"});

        Messages onlyPlayers = new Messages(List.of("only players"), true);

        Mockito.when(configManager.getOnlyPlayersMessage()).thenReturn(onlyPlayers);

        gpsCommand.onCommand(console, command, "gps", new String[]{"list"});

        assertEquals(0, listSubCommand.calls.size());
        Mockito.verify(messageService).send(Mockito.eq(console), Mockito.eq(onlyPlayers), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Без права подкоманда не выполняется")
    void noPermission() {

        listSubCommand.permission = "moongps.list";

        Mockito.when(permissionService.hasPermission(player, "moongps.list")).thenReturn(false);

        Messages noPermission = new Messages(List.of("no permission"), true);

        Mockito.when(configManager.getNoPermissionMessage()).thenReturn(noPermission);

        gpsCommand.onCommand(player, command, "gps", new String[]{"list"});

        assertEquals(0, listSubCommand.calls.size());
        Mockito.verify(messageService).send(Mockito.eq(player), Mockito.eq(noPermission), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Автодополнение первого аргумента: сначала метки, затем подкоманды")
    void completeFirstArgument() {

        List<String> completions = gpsCommand.onTabComplete(player, command, "gps", new String[]{""});
        assertEquals(List.of("shop", "bank", "list"), completions);

    }

    @Test
    @DisplayName("Скрытые подкоманды не подсказываются")
    void hiddenSubCommandNotCompleted() {

        List<String> completions = gpsCommand.onTabComplete(player, command, "gps", new String[]{"t"});
        assertEquals(List.of(), completions);

    }

    @Test
    @DisplayName("Автодополнение фильтруется по префиксу")
    void completeFiltered() {

        List<String> completions = gpsCommand.onTabComplete(player, command, "gps", new String[]{"sh"});
        assertEquals(List.of("shop"), completions);

    }

    @Test
    @DisplayName("Без права использования метки в автодополнение не попадают")
    void completeWithoutUsePermission() {

        Mockito.when(permissionService.hasPermission(player, "moongps.use")).thenReturn(false);

        List<String> completions = gpsCommand.onTabComplete(player, command, "gps", new String[]{""});

        assertFalse(completions.contains("shop"));
        assertTrue(completions.contains("list"));

    }

    @Test
    @DisplayName("Второй аргумент дополняется подкомандой")
    void completeSecondArgument() {

        listSubCommand.completions = List.of("1", "2");

        List<String> completions = gpsCommand.onTabComplete(player, command, "gps", new String[]{"list", ""});

        assertEquals(List.of("1", "2"), completions);

    }

    /**
     * Тестовая подкоманда: запоминает вызовы вместо настоящей работы.
     */
    private static final class FakeSubCommand implements GpsSubCommand {

        private final String name;
        private final boolean playerOnly;
        private final List<String[]> calls = new ArrayList<>();

        private @Nullable String permission;
        private List<String> completions = List.of();
        private boolean hidden;

        private FakeSubCommand(@NotNull String name, @Nullable String permission, boolean playerOnly, boolean hidden) {

            this.name = name;
            this.permission = permission;
            this.playerOnly = playerOnly;
            this.hidden = hidden;

        }

        @Override
        public boolean isHidden() {
            return hidden;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public @Nullable String getPermission() {
            return permission;
        }

        @Override
        public boolean isPlayerOnly() {
            return playerOnly;
        }

        @Override
        public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

            calls.add(args);
            return true;

        }

        @Override
        public @NotNull List<String> complete(@NotNull CommandContext context, String @NotNull [] args) {
            return completions;
        }
    }
    private GPSGoal goal(String name) {
        return new GPSGoal(name, 0.0D, 0.0D, 0.0D, "world", null);
    }
}
