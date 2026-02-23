package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.event.model.Location;
import ru.practicum.event.util.StateActionAdmin;

import java.time.LocalDateTime;

/**
 * DTO для обновления события администратором.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000, message = "Краткое описание события должно быть от 20 до 2000 символов")
    private String annotation;

    @Positive
    private Long category;

    @Size(min = 20, max = 7000, message = "Полное описание события должно быть от 20 до 7000 символов")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    private StateActionAdmin stateAction;

    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
    private String title;

    /**
     * Проверяет, есть ли аннотация.
     *
     * @return true если аннотация присутствует
     */
    public boolean hasAnnotation() {
        return annotation != null && !annotation.isBlank();
    }

    /**
     * Проверяет, есть ли категория.
     *
     * @return true если категория присутствует
     */
    public boolean hasCategory() {
        return category != null;
    }

    /**
     * Проверяет, есть ли описание.
     *
     * @return true если описание присутствует
     */
    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    /**
     * Проверяет, есть ли дата события.
     *
     * @return true если дата присутствует
     */
    public boolean hasEventDate() {
        return eventDate != null;
    }

    /**
     * Проверяет, есть ли местоположение.
     *
     * @return true если местоположение присутствует
     */
    public boolean hasLocation() {
        return location != null;
    }

    /**
     * Проверяет, есть ли признак платности.
     *
     * @return true если признак платности присутствует
     */
    public boolean hasPaid() {
        return paid != null;
    }

    /**
     * Проверяет, есть ли лимит участников.
     *
     * @return true если лимит присутствует
     */
    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    /**
     * Проверяет, есть ли признак модерации.
     *
     * @return true если признак модерации присутствует
     */
    public boolean hasRequestModeration() {
        return requestModeration != null;
    }

    /**
     * Проверяет, есть ли действие над состоянием.
     *
     * @return true если действие присутствует
     */
    public boolean hasStateAction() {
        return stateAction != null;
    }

    /**
     * Проверяет, есть ли заголовок.
     *
     * @return true если заголовок присутствует
     */
    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }
}