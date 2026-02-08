package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.category.model.Category;

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

    /**
     * Преобразует DTO в сущность категории.
     *
     * @return сущность категории
     */
    public Category mapToCategory() {
        return Category.builder()
                .name(this.name)
                .build();
    }
}