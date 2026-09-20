package org.ney.moongps.config.message;

import java.util.List;

/**
 * Список сообщений из конфига с учётом флага enabled.
 *
 * @param values  цветные строки сообщения
 * @param enabled включено ли сообщение
 */
public record Messages(List<String> values, boolean enabled) {

    private static final Messages DISABLED = new Messages(List.of(), false);

    /**
     * Создаёт выключенное сообщение.
     *
     * @return пустое сообщение
     */
    public static Messages disabled() {
        return DISABLED;
    }

    public boolean isEmpty() {
        return !enabled || values.isEmpty();
    }
}
