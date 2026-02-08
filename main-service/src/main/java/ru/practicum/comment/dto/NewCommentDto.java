package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

/**
 * DTO для создания нового комментария.
 * Содержит минимальный набор полей, необходимых для создания комментария.
 * Игнорируемые поля устанавливаются автоматически из контекста запроса.
 */
@Builder
@Getter
@Setter
public class NewCommentDto {

    /**
     * Идентификатор автора комментария.
     * Устанавливается автоматически из пути запроса, игнорируется при десериализации JSON.
     */
    @JsonIgnore
    private long author;

    /**
     * Идентификатор события, к которому относится комментарий.
     * Устанавливается автоматически из пути запроса, игнорируется при десериализации JSON.
     */
    @JsonIgnore
    private long event;

    /**
     * Текст комментария.
     * Должен содержать от 3 до 5000 символов.
     * Не может быть пустым или состоять только из пробелов.
     */
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Length(min = 3, max = 5000, message = "Текст комментария должен содержать от 3 до 5000 символов")
    private String text;

    /**
     * Дата и время создания комментария.
     * Устанавливается автоматически при создании DTO, игнорируется при десериализации JSON.
     */
    @JsonIgnore
    private final LocalDateTime created = LocalDateTime.now();

    /**
     * Объект автора комментария.
     * Устанавливается автоматически сервисом, игнорируется при десериализации JSON.
     */
    @JsonIgnore
    private User authorObj;

    /**
     * Объект события, к которому относится комментарий.
     * Устанавливается автоматически сервисом, игнорируется при десериализации JSON.
     */
    @JsonIgnore
    private Event eventObj;
}