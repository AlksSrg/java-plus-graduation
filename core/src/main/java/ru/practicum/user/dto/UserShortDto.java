package ru.practicum.user.dto;

import lombok.*;

/**
 * DTO для краткого представления данных пользователя.
 * <p>
 * Используется в случаях, когда необходима только базовая информация о пользователе.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {

    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Имя пользователя.
     */
    private String name;
}