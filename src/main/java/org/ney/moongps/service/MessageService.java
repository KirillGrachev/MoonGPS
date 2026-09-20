package org.ney.moongps.service;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.config.message.MoonTitle;
import org.ney.moongps.util.Placeholders;

/**
 * Сервис отправки сообщений игрокам и в консоль.
 * Префикс плагина не добавляется автоматически: он подставляется
 * туда, где в сообщении указана переменная {prefix}.
 */
public class MessageService {

    private static final String PLACEHOLDER_PREFIX = "{prefix}";

    private final ConfigManager configManager;

    public MessageService(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Отправляет сообщение в чат (с подстановкой {prefix} и плейсхолдеров).
     *
     * @param sender       получатель
     * @param messages     сообщение из конфига
     * @param placeholders плейсхолдеры
     */
    public void send(@NotNull CommandSender sender,
                     @NotNull Messages messages,
                     @NotNull Placeholders placeholders) {

        if (messages.isEmpty()) return;
        messages.values().forEach(line ->
                sender.sendMessage(withPrefix(placeholders.apply(line)))
        );

    }

    /**
     * Отправляет титул игроку.
     *
     * @param player       получатель
     * @param moonTitle    титул из конфига
     * @param placeholders плейсхолдеры
     */
    public void sendTitle(@NotNull Player player,
                          @NotNull MoonTitle moonTitle,
                          @NotNull Placeholders placeholders) {

        if (!moonTitle.isEnabled()) return;
        player.showTitle(moonTitle.asAdventure(
                placeholders.apply(moonTitle.title()),
                placeholders.apply(moonTitle.subtitle())
        ));

    }

    /**
     * Отправляет сообщение в action bar.
     *
     * @param player       получатель
     * @param messages     сообщение из конфига
     * @param placeholders плейсхолдеры
     */
    public void sendActionBar(@NotNull Player player,
                              @NotNull Messages messages,
                              @NotNull Placeholders placeholders) {

        if (messages.isEmpty()) return;
        player.sendActionBar(Component.text(placeholders.apply(messages.values().get(0))));

    }

    /**
     * Заменяет переменную {prefix} на префикс из конфига.
     *
     * @param text текст сообщения
     * @return текст с подставленным префиксом
     */
    private @NotNull String withPrefix(@NotNull String text) {
        return text.replace(PLACEHOLDER_PREFIX, configManager.getPrefix());
    }
}
