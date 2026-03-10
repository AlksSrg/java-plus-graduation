package ru.practicum.category.controller.public_;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

/**
 * Публичный контроллер для операций с категориями.
 * Предоставляет endpoints для получения категорий (список и по идентификатору).
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService categoryService;

    /**
     * Возвращает список категорий с пагинацией.
     *
     * @param from индекс первого элемента (начиная с 0)
     * @param size количество элементов на странице
     * @return список категорий
     */
    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(defaultValue = "10") @Positive int size) {
        log.debug("Public: запрос списка категорий from={}, size={}", from, size);
        return categoryService.getAll(from, size);
    }

    /**
     * Возвращает категорию по идентификатору.
     *
     * @param catId идентификатор категории
     * @return данные категории
     */
    @GetMapping("/{catId}")
    public CategoryDto get(@PathVariable @Positive long catId) {
        log.debug("Public: запрос категории id={}", catId);
        return categoryService.get(catId);
    }
}