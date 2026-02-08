package ru.practicum.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.category.model.Category;

/**
 * DTO для представления категории.
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

    /**
     * Создает DTO из сущности категории.
     *
     * @param category сущность категории
     * @return DTO категории
     */
    public static CategoryDto mapFromCategory(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    /**
     * Преобразует DTO в сущность категории.
     *
     * @return сущность категории
     */
    public Category mapToCategory() {
        return Category.builder()
                .id(this.id)
                .name(this.name)
                .build();
    }
}