package org.ney.moongps.command.sub;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.command.CommandContext;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.service.MessageService;
import org.ney.moongps.service.NavigationService;
import org.ney.moongps.util.Placeholders;

import java.util.List;

class ReloadCommandTest {

    private MoonGPS plugin;
    private ConfigManager configManager;
    private MessageService messageService;
    private ReloadCommand reloadCommand;
    private CommandSender sender;

    @BeforeEach
    void setUp() {

        plugin = Mockito.mock(MoonGPS.class);
        configManager = Mockito.mock(ConfigManager.class);
        messageService = Mockito.mock(MessageService.class);

        NavigationService navigationService = Mockito.mock(NavigationService.class);

        Mockito.when(plugin.getNavigationService()).thenReturn(navigationService);
        Mockito.when(navigationService.getActiveSessionsCount()).thenReturn(3);
        Mockito.when(plugin.reloadPlugin()).thenReturn(19);

        reloadCommand = new ReloadCommand(plugin, configManager, messageService);
        sender = Mockito.mock(CommandSender.class);

    }

    @Test
    @DisplayName("Перезагрузка плагина и отчёт о метках и сессиях")
    void reloadsAndReports() {

        Messages success = new Messages(List.of("reloaded"), true);

        Mockito.when(configManager.getReloadSuccessMessage()).thenReturn(success);

        reloadCommand.execute(CommandContext.of(sender), new String[0]);

        Mockito.verify(plugin).reloadPlugin();
        Mockito.verify(messageService).send(Mockito.eq(sender), Mockito.eq(success), Mockito.any(Placeholders.class));

    }

    @Test
    @DisplayName("Без сервиса навигации перезагрузка не падает")
    void reloadWithoutNavigationService() {

        Mockito.when(plugin.getNavigationService()).thenReturn(null);

        reloadCommand.execute(CommandContext.of(sender), new String[0]);

        Mockito.verify(plugin).reloadPlugin();

    }
}
