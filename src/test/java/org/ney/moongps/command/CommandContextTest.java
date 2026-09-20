package org.ney.moongps.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandContextTest {

    @Test
    @DisplayName("Игрок попадет в контекст игроком")
    void playerContext() {

        Player player = Mockito.mock(Player.class);
        CommandContext context = CommandContext.of(player);

        assertTrue(context.isPlayer());
        assertSame(player, context.player());
        assertSame(player, context.sender());

    }

    @Test
    @DisplayName("Консоль попадает в контекст без игрока")
    void consoleContext() {

        CommandSender console = Mockito.mock(CommandSender.class);
        CommandContext context = CommandContext.of(console);

        assertFalse(context.isPlayer());
        assertSame(console, context.sender());

    }
}
