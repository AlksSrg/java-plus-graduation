package ru.practicum.comment.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Параметры для получения списка комментариев.
 * Содержит все возможные критерии фильтрации и пагинации.
 */
@Builder
@Getter
@Setter
public class CommentGetParam {
    /**
     * Идентификатор события, для которого запрашиваются комментарии.
     */
    private long eventId;

    /**
     * Список идентификаторов авторов для фильтрации комментариев.
     */
    private List<Long> authorIds;

    /**
     * Порядок сортировки комментариев.
     */
    private SortOrder sortBy;

    /**
     * Количество элементов, которое нужно пропустить для пагинации.
     */
    private int from;

    /**
     * Количество элементов, которое нужно вернуть.
     */
    private int size;
}