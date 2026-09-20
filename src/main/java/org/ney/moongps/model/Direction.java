package org.ney.moongps.model;

import org.jetbrains.annotations.NotNull;

/**
 * Результат расчёта направления до метки.
 *
 * @param relative относительное направление (прямо, налево, направо, назад)
 * @param compass  сторона света
 */
public record Direction(@NotNull Relative relative,
                        @NotNull Compass compass) {

    /**
     * Относительное направление относительно взгляда игрока.
     */
    public enum Relative {

        AHEAD,
        LEFT,
        RIGHT,
        BEHIND
    }

    /**
     * Сторона света (8 направлений компаса).
     */
    public enum Compass {

        NORTH("↑"),
        NORTH_EAST("↗"),
        EAST("→"),
        SOUTH_EAST("↘"),
        SOUTH("↓"),
        SOUTH_WEST("↙"),
        WEST("←"),
        NORTH_WEST("↖");

        private final String arrow;

        Compass(@NotNull String arrow) {
            this.arrow = arrow;
        }

        public @NotNull String getArrow() {
            return arrow;
        }
    }
}
