package org.ney.moongps.util;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Утилита проверки названий меток.
 * Название хранится как ключ YAML, поэтому допускаются только безопасные символы.
 */
public class GoalNameValidator {

    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    private GoalNameValidator() {

    }

    /**
     * Проверяет корректность названия метки.
     *
     * @param name название метки
     * @return true если название допустимо
     */
    public static boolean isValid(@Nullable String name) {

        if (name == null || name.isBlank()) return false;
        return NAME_PATTERN.matcher(name).matches();

    }
}
