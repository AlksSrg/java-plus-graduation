package ru.practicum.user.dto;

import lombok.*;

/**
 * Краткое DTO для представления пользователя.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {

    private Long id;
    private String name;
}