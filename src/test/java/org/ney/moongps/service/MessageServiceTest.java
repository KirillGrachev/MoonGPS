package org.ney.moongps.service;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.config.message.MoonTitle;
import org.ney.moongps.util.Placeholders;

import java.util.List;

class MessageServiceTest {

    private ConfigManager configManager;
    private MessageService messageService;
    private CommandSender sender;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        Mockito.when(configManager.getPrefix()).thenReturn("[GPS] ");

        messageService = new MessageService(configManager);
        sender = Mockito.mock(CommandSender.class);

    }

    @Test
    @DisplayName("Префикс подставляется туда, где указана переменная {prefix}")
    void prefixSubstitutedWhereVariableUsed() {

        Messages messages = new Messages(List.of("{prefix}Mark {gps} not found!"), true);

        messageService.send(sender, messages, Placeholders.create().add("gps", "shop"));

        Mockito.verify(sender).sendMessage("[GPS] Mark shop not found!");

    }

    @Test
    @DisplayName("Без переменной {prefix} сообщение отправляется как есть")
    void messageWithoutPrefixVariableKept() {

        Messages messages = new Messages(List.of("Just a line"), true);

        messageService.send(sender, messages, Placeholders.create());

        Mockito.verify(sender).sendMessage("Just a line");

    }

    @Test
    @DisplayName("Титул отправляется игроку, выключенный титул - нет")
    void titleSent() {

        Player player = Mockito.mock(Player.class);
        MoonTitle enabled = new MoonTitle("head", "sub", 0, 40, 10);

        messageService.sendTitle(player, enabled, Placeholders.create());
        messageService.sendTitle(player, MoonTitle.disabled(), Placeholders.create());

        Mockito.verify(player, Mockito.times(1)).showTitle(Mockito.any(Title.class));

    }

    @Test
    @DisplayName("Action bar отправляется одной строкой")
    void actionBarSent() {

        Player player = Mockito.mock(Player.class);

        messageService.sendActionBar(player, new Messages(List.of("bar line"), true), Placeholders.create());
        messageService.sendActionBar(player, Messages.disabled(), Placeholders.create());

        Mockito.verify(player, Mockito.times(1)).sendActionBar(Mockito.any(Component.class));

    }

    @Test
    @DisplayName("Выключенное сообщение не отправляется")
    void disabledMessageNotSent() {

        messageService.send(sender, Messages.disabled(), Placeholders.create());
        Mockito.verify(sender, Mockito.never()).sendMessage(Mockito.anyString());

    }
}
