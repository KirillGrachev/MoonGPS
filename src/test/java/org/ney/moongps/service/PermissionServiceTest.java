package org.ney.moongps.service;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ney.moongps.config.ConfigManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionServiceTest {

    private ConfigManager configManager;
    private PermissionService permissionService;
    private CommandSender sender;

    @BeforeEach
    void setUp() {

        configManager = Mockito.mock(ConfigManager.class);
        permissionService = new PermissionService(configManager);
        sender = Mockito.mock(CommandSender.class);

    }

    @Test
    @DisplayName("Выключенная система прав пропускает всех")
    void permissionsDisabled() {

        Mockito.when(configManager.arePermissionsEnabled()).thenReturn(false);
        Mockito.when(sender.hasPermission("moongps.use")).thenReturn(false);

        assertTrue(permissionService.hasPermission(sender, "moongps.use"));

    }

    @Test
    @DisplayName("Пустое право не требует проверки")
    void emptyPermissionAllowed() {

        Mockito.when(configManager.arePermissionsEnabled()).thenReturn(true);

        assertTrue(permissionService.hasPermission(sender, null));
        assertTrue(permissionService.hasPermission(sender, "  "));

    }

    @Test
    @DisplayName("op_bypass пропускает операторов и консоль")
    void opBypass() {

        CommandSender op = Mockito.mock(CommandSender.class);
        ConsoleCommandSender console = Mockito.mock(ConsoleCommandSender.class);

        Mockito.when(configManager.arePermissionsEnabled()).thenReturn(true);
        Mockito.when(configManager.isOpBypassEnabled()).thenReturn(true);
        Mockito.when(op.isOp()).thenReturn(true);
        Mockito.when(op.hasPermission("moongps.set")).thenReturn(false);

        assertTrue(permissionService.hasPermission(op, "moongps.set"));
        assertTrue(permissionService.hasPermission(console, "moongps.set"));

    }

    @Test
    @DisplayName("Без op_bypass оператор проверяется как обычно")
    void opWithoutBypass() {

        CommandSender op = Mockito.mock(CommandSender.class);

        Mockito.when(configManager.arePermissionsEnabled()).thenReturn(true);
        Mockito.when(configManager.isOpBypassEnabled()).thenReturn(false);
        Mockito.when(op.isOp()).thenReturn(true);
        Mockito.when(op.hasPermission("moongps.set")).thenReturn(false);

        assertFalse(permissionService.hasPermission(op, "moongps.set"));

    }

    @Test
    @DisplayName("Включённая система прав спрашивает Bukkit")
    void permissionsEnabled() {

        Mockito.when(configManager.arePermissionsEnabled()).thenReturn(true);
        Mockito.when(sender.hasPermission("moongps.set")).thenReturn(true);
        Mockito.when(sender.hasPermission("moongps.delete")).thenReturn(false);

        assertTrue(permissionService.hasPermission(sender, "moongps.set"));
        assertFalse(permissionService.hasPermission(sender, "moongps.delete"));

    }
}
