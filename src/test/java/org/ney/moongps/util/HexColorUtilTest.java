package org.ney.moongps.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexColorUtilTest {

    @Test
    @DisplayName("Пустая и null строка возвращают пустую строку")
    void colorReturnsEmptyForNullOrEmpty() {

        assertEquals("", HexColorUtil.color(null));
        assertEquals("", HexColorUtil.color(""));

    }

    @Test
    @DisplayName("HEX-код конвертируется в формат &x&R&R&G&G&B&B")
    void colorConvertsHexCode() {

        String result = HexColorUtil.color("#ff0000Text");
        assertEquals("§x§f§f§0§0§0§0Text", result);

    }

    @Test
    @DisplayName("Обычные цветовые коды & конвертируются")
    void colorConvertsLegacyCodes() {

        String result = HexColorUtil.color("&aGreen &cRed");

        assertTrue(result.contains("§a"));
        assertTrue(result.contains("§c"));

    }

    @Test
    @DisplayName("Некорректный HEX-код остаётся без изменений")
    void colorKeepsInvalidHexCode() {

        String result = HexColorUtil.color("#zzzzzz");
        assertEquals("#zzzzzz", result);

    }

    @Test
    @DisplayName("Несколько HEX-кодов в одной строке")
    void colorConvertsMultipleHexCodes() {

        String result = HexColorUtil.color("#42fffc A #ff0000 B");

        assertTrue(result.contains("§x§4§2§f§f§f§c"));
        assertTrue(result.contains("§x§f§f§0§0§0§0"));

    }
}
