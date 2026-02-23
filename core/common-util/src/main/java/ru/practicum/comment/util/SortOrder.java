package ru.practicum.comment.util;

/**
 * Возможные варианты сортировки комментариев.
 */
public enum SortOrder {
    /**
     * Сортировка по дате создания (новые сначала).
     */
    CREATED,

    /**
     * Сортировка по имени автора.
     */
    AUTHOR
}