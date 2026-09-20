package org.ney.moongps.config.type;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.config.message.MoonTitle;

/**
 * Настройки вывода навигационной информации.
 * Каждый канал включается своим флагом, комбинации свободные.
 *
 * @param titleEnabled     титул + субтитры
 * @param actionBarEnabled строка над хотбаром
 * @param bossBarEnabled   boss bar вверху экрана
 * @param chatEnabled      дублирование в чат
 * @param title            титул навигации
 * @param actionBar        текст action bar
 * @param bossBar          текст boss bar
 * @param chat             текст чата
 * @param barColor         цвет boss bar
 * @param barStyle         стиль деления boss bar
 * @param progress         поведение заполнения boss bar
 */
public record DisplaySettings(boolean titleEnabled,
                              boolean actionBarEnabled,
                              boolean bossBarEnabled,
                              boolean chatEnabled,
                              MoonTitle title,
                              Messages actionBar,
                              Messages bossBar,
                              Messages chat,
                              BarColor barColor,
                              BarStyle barStyle,
                              BossBarProgress progress) {
}
