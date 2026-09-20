package org.ney.moongps.util;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Утилита для сборки и подстановки плейсхолдеров в сообщения.
 */
public class Placeholders {

    private final Map<String, String> values = new LinkedHashMap<>();

    private Placeholders() {

    }

    /**
     * Создаёт пустой набор плейсхолдеров.
     *
     * @return новый экземпляр
     */
    public static @NotNull Placeholders create() {
        return new Placeholders();
    }

    /**
     * Добавляет плейсхолдер и его значение.
     *
     * @param key   имя плейсхолдера без фигурных скобок
     * @param value значение
     * @return этот же экземпляр (для цепочки вызовов)
     */
    public @NotNull Placeholders add(@NotNull String key, @NotNull Object value) {

        values.put(key, String.valueOf(value));
        return this;

    }

    /**
     * Подставляет все плейсхолдеры в текст.
     *
     * @param text исходный текст
     * @return текст с подставленными значениями
     */
    public @NotNull String apply(@NotNull String text) {

        String result = text;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return result;

    }
}
