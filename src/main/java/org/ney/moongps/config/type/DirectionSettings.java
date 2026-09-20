package org.ney.moongps.config.type;

import java.util.Map;

/**
 * Настройки расчёта и оформления направления до метки.
 *
 * @param mode             режим расчёта направления
 * @param aheadThreshold   cos(угла), при превышении которого направление считается "прямо"
 * @param behindThreshold  cos(угла), ниже которого направление считается "назад"
 * @param format           формат оформления направления ({arrow}, {name})
 * @param relativeNames    названия относительных направлений (AHEAD, LEFT, RIGHT, BEHIND)
 * @param compassSymbols   названия сторон света (NORTH, NORTH_EAST, ...)
 */
public record DirectionSettings(DirectionMode mode,
                                double aheadThreshold,
                                double behindThreshold,
                                String format,
                                Map<String, String> relativeNames,
                                Map<String, String> compassSymbols) {
}
