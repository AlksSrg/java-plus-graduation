package ru.practicum.category.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

/**
 * Контроллер для административных операций с категориями.
 */
@Validated
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {
    private final CategoryService categoryService;

    /**
     * Создает новую категорию.
     *
     * @param categoryDto данные для создания категории
     * @return созданная категория
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid NewCategoryDto categoryDto) {
        return categoryService.create(categoryDto);
    }

    /**
     * Обновляет существующую категорию.
     *
     * @param categoryDto данные для обновления категории
     * @param catId       идентификатор категории
     * @return обновленная категория
     */
    @PatchMapping("/{catId}")
    public CategoryDto update(@RequestBody @Valid CategoryDto categoryDto,
                              @PathVariable @Positive Long catId) {
        categoryDto.setId(catId);
        return categoryService.update(categoryDto);
    }

    /**
     * Удаляет категорию по идентификатору.
     *
     * @param catId идентификатор категории для удаления
     */
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long catId) {
        categoryService.delete(catId);
    }
}