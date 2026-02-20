package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * DTO для создания нового комментария.
 * Содержит минимальный набор полей, необходимых для создания комментария.
 * Игнорируемые поля устанавливаются автоматически из контекста запроса.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {

    /**
     * Идентификатор автора комментария.
     * Устанавливается автоматически из пути запроса, игнорируется при десериализации JSON.
     */
    private Long authorId;

    /**
     * Идентификатор события, к которому относится комментарий.
     * Должен быть передан в теле запроса или из пути.
     */
    @NotNull(message = "ID события не может быть пустым")
    @Positive(message = "ID события должен быть положительным")
    private Long eventId;

    /**
     * Текст комментария.
     * Должен содержать от 3 до 5000 символов.
     * Не может быть пустым или состоять только из пробелов.
     */
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Length(min = 3, max = 5000, message = "Текст комментария должен содержать от 3 до 5000 символов")
    private String text;
}