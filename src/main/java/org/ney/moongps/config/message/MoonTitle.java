package org.ney.moongps.config.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Титул из конфига: заголовок, субтитр и тайминги.
 *
 * @param title    заголовок (null - титул выключен)
 * @param subtitle субтитр
 * @param fadeIn   появление (в тиках)
 * @param stay     показ (в тиках)
 * @param fadeOut  исчезновение (в тиках)
 */
public record MoonTitle(@Nullable String title,
                        String subtitle,
                        int fadeIn,
                        int stay,
                        int fadeOut) {

    private static final int TICKS_PER_SECOND = 20;

    /**
     * Создаёт выключенный титул.
     *
     * @return титул без содержимого
     */
    public static MoonTitle disabled() {
        return new MoonTitle(null, "", 0, 0, 0);
    }

    public boolean isEnabled() {
        return title != null;
    }

    /**
     * Собирает adventure-титул с подставленными значениями.
     *
     * @param title    заголовок с подставленными плейсхолдерами
     * @param subtitle субтитр с подставленными плейсхолдерами
     * @return готовый к отправке титул
     */
    public Title asAdventure(String title, String subtitle) {

        Times times = Times.times(
                toDuration(fadeIn),
                toDuration(stay),
                toDuration(fadeOut)
        );
        return Title.title(
                Component.text(title),
                Component.text(subtitle),
                times
        );

    }

    private static Duration toDuration(int ticks) {
        return Duration.ofMillis(ticks * 1000L / TICKS_PER_SECOND);
    }
}
