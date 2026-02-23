package ru.practicum.user.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Класс параметров для получения пользователей.
 * Содержит параметры фильтрации и пагинации.
 */
@Builder
@Getter
@Setter
public class UserGetParam {

    /**
     * Список идентификаторов пользователей для фильтрации.
     * Если null или пустой - возвращаются все пользователи.
     */
    private List<Long> ids;

    /**
     * Количество элементов для пропуска (пагинация).
     */
    @Builder.Default
    private Integer from = 0;

    /**
     * Количество элементов на странице (пагинация).
     */
    @Builder.Default
    private Integer size = 10;
}