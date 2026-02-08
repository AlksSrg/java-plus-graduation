package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * DTO для обновления подборки событий.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {
    private List<Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Длина заголовка должна составлять от 1 до 50 символов")
    private String title;
}