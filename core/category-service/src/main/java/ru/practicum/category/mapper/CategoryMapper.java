package ru.practicum.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

/**
 * Маппер для преобразования между сущностью Category и DTO.
 */
@UtilityClass
public class CategoryMapper {

    /**
     * Преобразует сущность в DTO.
     *
     * @param category сущность категории
     * @return DTO категории
     */
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    /**
     * Преобразует DTO создания в сущность.
     *
     * @param newCategoryDto DTO для создания
     * @return сущность категории
     */
    public Category toEntity(NewCategoryDto newCategoryDto) {
        if (newCategoryDto == null) {
            return null;
        }
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    /**
     * Обновляет существующую сущность из DTO.
     *
     * @param categoryDto DTO с данными
     * @param category    существующая сущность
     * @return обновленная сущность
     */
    public Category updateEntity(CategoryDto categoryDto, Category category) {
        if (categoryDto.getName() != null) {
            category.setName(categoryDto.getName());
        }
        return category;
    }
}