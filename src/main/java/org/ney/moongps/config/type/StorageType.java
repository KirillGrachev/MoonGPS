package org.ney.moongps.config.type;

/**
 * Формат хранения меток.
 */
public enum StorageType {

    /** Файл goals.yml в папке плагина. */
    YAML,

    /** Таблица MySQL во внешней базе данных. */
    MYSQL,

    /** Файловая база в папке плагина (H2, синтаксис MySQL). */
    FILE;

    /**
     * Безопасно получает тип по его названию из конфига.
     *
     * @param value    название типа
     * @param fallback значение по умолчанию
     * @return найденный тип или fallback
     */
    public static StorageType of(String value, StorageType fallback) {

        if (value == null || value.isBlank()) return fallback;

        for (StorageType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) return type;
        }

        return fallback;

    }
}
