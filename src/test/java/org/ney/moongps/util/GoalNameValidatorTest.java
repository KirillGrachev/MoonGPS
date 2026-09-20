package org.ney.moongps.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalNameValidatorTest {

    @Test
    @DisplayName("Корректные названия меток принимаются")
    void validNamesAccepted() {

        assertTrue(GoalNameValidator.isValid("shop"));
        assertTrue(GoalNameValidator.isValid("BattlePass_1"));
        assertTrue(GoalNameValidator.isValid("pvp-arena"));

    }

    @Test
    @DisplayName("Некорректные названия меток отклоняются")
    void invalidNamesRejected() {

        assertFalse(GoalNameValidator.isValid(null));
        assertFalse(GoalNameValidator.isValid(""));
        assertFalse(GoalNameValidator.isValid("  "));
        assertFalse(GoalNameValidator.isValid("my mark"));
        assertFalse(GoalNameValidator.isValid("mark.name"));
        assertFalse(GoalNameValidator.isValid("mark/1"));
        assertFalse(GoalNameValidator.isValid("a".repeat(33)));

    }
}
