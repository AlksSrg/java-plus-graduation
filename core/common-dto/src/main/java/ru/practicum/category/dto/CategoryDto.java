package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
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

    private Long id;

    @NotBlank(message = "Category name cannot be empty")
    @Size(min = 1, max = 50, message = "Category name must contain from {min} to {max} characters")
    private String name;
}