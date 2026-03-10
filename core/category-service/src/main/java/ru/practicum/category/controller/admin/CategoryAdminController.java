package ru.practicum.category.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

/**
 * Контроллер для административных операций с категориями.
 * Предоставляет endpoints для создания, обновления и удаления категорий.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    /**
     * Создаёт новую категорию.
     *
     * @param categoryDto данные для создания категории (имя)
     * @return созданная категория с присвоенным идентификатором
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid NewCategoryDto categoryDto) {
        log.debug("Admin: создание категории {}", categoryDto);
        return categoryService.create(categoryDto);
    }

    /**
     * Обновляет существующую категорию.
     *
     * @param categoryDto обновлённые данные категории (имя)
     * @param catId       идентификатор обновляемой категории
     * @return обновлённая категория
     */
    @PatchMapping("/{catId}")
    public CategoryDto update(@RequestBody @Valid CategoryDto categoryDto,
                              @PathVariable @Positive Long catId) {
        log.debug("Admin: обновление категории id={}, данные={}", catId, categoryDto);
        categoryDto.setId(catId);
        return categoryService.update(categoryDto);
    }

    /**
     * Удаляет категорию по идентификатору.
     *
     * @param catId идентификатор удаляемой категории
     */
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long catId) {
        log.debug("Admin: удаление категории id={}", catId);
        categoryService.delete(catId);
    }
}