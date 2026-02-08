package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.user.model.User;

/**
 * DTO для обновления существующего комментария.
 * Содержит только те поля, которые могут быть изменены пользователем.
 * Игнорируемые поля устанавливаются автоматически из контекста запроса.
 */
@Builder
@Getter
@Setter
public class UpdateCommentDto {

    /**
     * Идентификатор автора комментария.
     * Устанавливается автоматически из пути запроса, игнорируется при десериализации JSON.
     */
    @JsonIgnore
    private long author;

    /**
     * Идентификатор обновляемого комментария.
     * Устанавливается автоматически из пути запроса, игнорируется при десериализации JSON.
     */
    @JsonIgnore
    private long commentId;

    /**
     * Новый текст комментария.
     * Должен содержать от 3 до 5000 символов.
     * Не может быть пустым или состоять только из пробелов.
     */
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 3, max = 5000, message = "Текст комментария должен содержать от 3 до 5000 символов")
    private String text;

    /**
     * Объект автора комментария.
     * Устанавливается автоматически сервисом, игнорируется при десериализации JSON.
     */
    @JsonIgnore
    private User authorObj;
}