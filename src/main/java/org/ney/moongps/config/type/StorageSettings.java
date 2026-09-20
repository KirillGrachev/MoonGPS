package org.ney.moongps.config.type;

/**
 * Выбранный формат хранения меток и его настройки.
 *
 * @param type формат хранения
 * @param sql  настройки MySQL (используются только при type = SQL)
 */
public record StorageSettings(StorageType type, SqlSettings sql) {
}
