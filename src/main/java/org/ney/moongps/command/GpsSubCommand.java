package org.ney.moongps.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Контракт подкоманды плагина (/gps <подкоманда>).
 */
public interface GpsSubCommand {

    /**
     * Возвращает название подкоманды.
     *
     * @return название
     */
    @NotNull String getName();

    /**
     * Возвращает право доступа к подкоманде.
     *
     * @return название права или null, если право не требуется
     */
    @Nullable String getPermission();

    /**
     * Показывает, доступна ли подкоманда только игрокам.
     *
     * @return true если консоль не может использовать подкоманду
     */
    boolean isPlayerOnly();

    /**
     * Скрывает подкоманду из автодополнения (служебные действия).
     *
     * @return true если подкоманда не подсказывается
     */
    default boolean isHidden() {
        return false;
    }

    /**
     * Выполняет подкоманду.
     *
     * @param context контекст вызова
     * @param args    аргументы после названия подкоманды
     * @return true если команда обработана
     */
    boolean execute(@NotNull CommandContext context, @NotNull String[] args);

    /**
     * Возвращает варианты автодополнения подкоманды.
     *
     * @param context контекст вызова
     * @param args    аргументы после названия подкоманды
     * @return варианты автодополнения
     */
    default @NotNull List<String> complete(@NotNull CommandContext context, @NotNull String[] args) {
        return List.of();
    }
}
