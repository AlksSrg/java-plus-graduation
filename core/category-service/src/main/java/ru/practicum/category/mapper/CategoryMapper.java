package ru.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

/**
 * Маппер для преобразования между сущностью Category и DTO.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Преобразует DTO создания в сущность.
     *
     * @param newCategoryDto DTO для создания
     * @return сущность категории
     */
    @Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto newCategoryDto);

    /**
     * Преобразует сущность в DTO.
     *
     * @param category сущность категории
     * @return DTO категории
     */
    CategoryDto toDto(Category category);

    /**
     * Обновляет существующую сущность из DTO.
     *
     * @param dto      DTO с данными для обновления
     * @param category существующая сущность
     */
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(NewCategoryDto dto, @MappingTarget Category category);
}