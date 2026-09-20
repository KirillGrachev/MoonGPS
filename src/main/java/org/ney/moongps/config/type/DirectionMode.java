package org.ney.moongps.config.type;

/**
 * Режим отображения направления до метки.
 */
public enum DirectionMode {

    /** Прямо / налево / направо / назад относительно взгляда игрока. */
    RELATIVE,

    /** 8 сторон света (компас). */
    COMPASS;

    /**
     * Безопасно получает режим по его названию из конфига.
     *
     * @param value    название режима
     * @param fallback значение по умолчанию
     * @return найденный режим или fallback
     */
    public static DirectionMode of(String value, DirectionMode fallback) {

        if (value == null || value.isBlank()) return fallback;

        for (DirectionMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value.trim())) return mode;
        }

        return fallback;

    }
}
