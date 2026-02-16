package ru.practicum.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO для представления категории.
 * Используется для передачи данных между микросервисами.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    @Positive
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Size(min = 1, max = 50)
    @NotBlank
    private String name;
}