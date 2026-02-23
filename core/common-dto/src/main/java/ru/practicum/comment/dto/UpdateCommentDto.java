package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления существующего комментария.
 * Содержит только те поля, которые могут быть изменены пользователем.
 * Игнорируемые поля устанавливаются автоматически из контекста запроса.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentDto {

    /**
     * Идентификатор обновляемого комментария.
     * Устанавливается автоматически из пути запроса.
     */
    private Long commentId;

    /**
     * Идентификатор автора комментария.
     * Устанавливается автоматически из пути запроса.
     */
    private Long authorId;

    /**
     * Новый текст комментария.
     * Должен содержать от 3 до 5000 символов.
     * Не может быть пустым или состоять только из пробелов.
     */
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 3, max = 5000, message = "Текст комментария должен содержать от 3 до 5000 символов")
    private String text;
}