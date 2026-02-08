package ru.practicum.comment.dto;

import lombok.*;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

/**
 * DTO для представления комментария в API.
 * Содержит полную информацию о комментарии, включая связанные сущности.
 * Используется для возврата данных в ответах API.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    /**
     * Уникальный идентификатор комментария.
     */
    private Long id;

    /**
     * Автор комментария в формате DTO.
     * Содержит основную информацию о пользователе.
     */
    private UserDto author;

    /**
     * Событие, к которому относится комментарий, в формате короткого DTO.
     * Содержит основную информацию о событии.
     */
    private EventShortDto event;

    /**
     * Дата и время создания комментария.
     */
    private LocalDateTime created;

    /**
     * Текст комментария.
     */
    private String text;
}