package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * DTO для создания новой категории.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {

    @Length(min = 1, max = 50)
    @NotBlank
    private String name;
}