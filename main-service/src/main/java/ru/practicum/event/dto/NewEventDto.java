package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Location;
import ru.practicum.user.model.User;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Широта и долгота должны быть заполнены")
    private Location location;

    @Builder.Default
    private boolean paid = false;

    @Builder.Default
    @PositiveOrZero
    private int participantLimit = 0;

    @Builder.Default
    private boolean requestModeration = true;

    @NotBlank(message = "Заголовок события должен быть заполнен")
    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
    private String title;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long initiator;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Category categoryObject;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User initiatorObject;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Builder.Default
    private LocalDateTime createdOn = LocalDateTime.now();
}