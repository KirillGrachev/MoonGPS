package org.ney.moongps.config.type;

import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.Nullable;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.config.message.MoonTitle;

/**
 * Настройки момента достижения метки.
 * Каждый канал вывода включается флагом enabled у своего сообщения.
 *
 * @param distance  расстояние (в блоках), на котором метка считается достигнутой
 * @param title     титул о достижении
 * @param messages  сообщение в чат о достижении
 * @param actionBar сообщение в action bar о достижении
 * @param bossBar   временная полоса о достижении
 * @param barColor  цвет временной полосы
 * @param barStyle  стиль временной полосы
 * @param showTime  время показа временной полосы в тиках
 * @param sound     звук достижения (null - выключен)
 */
public record ReachSettings(double distance,
                            MoonTitle title,
                            Messages messages,
                            Messages actionBar,
                            Messages bossBar,
                            BarColor barColor,
                            BarStyle barStyle,
                            long showTime,
                            @Nullable SoundEffect sound) {

    /**
     * Звук, проигрываемый игроку при достижении метки.
     *
     * @param sound  bukkit-звук
     * @param volume громкость
     * @param pitch  высота
     */
    public record SoundEffect(Sound sound, float volume, float pitch) {
    }
}
