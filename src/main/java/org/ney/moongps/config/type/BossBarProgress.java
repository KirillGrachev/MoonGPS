package org.ney.moongps.config.type;

/**
 * Поведение заполнения boss bar во время навигации.
 */
public enum BossBarProgress {

    /** Полоса заполняется по мере приближения к метке. */
    DISTANCE,

    /** Полоса всегда заполнена целиком. */
    FULL;

    /**
     * Безопасно получает тип прогресса по его названию из конфига.
     *
     * @param value    название типа
     * @param fallback значение по умолчанию
     * @return найденный тип или fallback
     */
    public static BossBarProgress of(String value, BossBarProgress fallback) {

        if (value == null || value.isBlank()) return fallback;

        for (BossBarProgress progress : values()) {
            if (progress.name().equalsIgnoreCase(value.trim())) return progress;
        }

        return fallback;

    }
}
