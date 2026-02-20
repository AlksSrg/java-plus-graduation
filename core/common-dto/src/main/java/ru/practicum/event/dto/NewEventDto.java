package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.practicum.event.model.Location;

import java.time.LocalDateTime;

/**
 * DTO для создания нового события.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Краткое описание должно быть заполнено")
    @Size(min = 20, max = 2000, message = "Краткое описание события должно быть от 20 до 2000 символов")
    private String annotation;

    @NotNull(message = "id категории обязательно для ввода")
    @Positive(message = "id категории должно быть больше 0")
    private Long category;

    @NotBlank(message = "Полное описание должно быть заполнено")
    @Size(min = 20, max = 7000, message = "Полное описание события должно быть от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Дата и время должны быть заполнены")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Широта и долгота должны быть заполнены")
    private Location location;

    private boolean paid;

    @PositiveOrZero
    private int participantLimit;

    private boolean requestModeration;

    @NotBlank(message = "Заголовок события должен быть заполнен")
    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
    private String title;
}