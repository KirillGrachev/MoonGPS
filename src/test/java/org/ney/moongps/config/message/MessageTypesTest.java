package org.ney.moongps.config.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageTypesTest {

    @Test
    @DisplayName("Выключенное сообщение пусто")
    void messagesDisabled() {

        Messages messages = Messages.disabled();

        assertTrue(messages.isEmpty());
        assertTrue(new Messages(List.of(), true).isEmpty());
        assertFalse(new Messages(List.of("text"), true).isEmpty());

    }

    @Test
    @DisplayName("Выключенный титул не отображается")
    void titleDisabled() {

        MoonTitle title = MoonTitle.disabled();

        assertFalse(title.isEnabled());
        assertTrue(new MoonTitle("title", "sub", 0, 40, 10).isEnabled());

    }

    @Test
    @DisplayName("Титул собирается в adventure с таймингами в тиках")
    void titleAsAdventure() {

        MoonTitle moonTitle = new MoonTitle("head", "sub", 10, 40, 20);
        Title title = moonTitle.asAdventure("head", "sub");

        assertEquals(Component.text("head"), title.title());
        assertEquals(Component.text("sub"), title.subtitle());
        assertEquals(Duration.ofMillis(500), title.times().fadeIn());
        assertEquals(Duration.ofMillis(2000), title.times().stay());
        assertEquals(Duration.ofMillis(1000), title.times().fadeOut());

    }
}
