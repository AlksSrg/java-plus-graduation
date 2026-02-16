package ru.practicum.category.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.service.CategoryService;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.feignclients.client.EventClient;

import java.util.List;

/**
 * Реализация сервиса для работы с категориями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventClient eventClient;

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        log.info("Getting all categories with from={}, size={}", from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto get(long catId) {
        log.info("Getting category with id: {}", catId);
        Category category = getCategoryById(catId);
        return CategoryMapper.toDto(category);
    }

    @Override
    public Category getCategoryById(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundResource("Категория", catId));
    }

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto categoryDto) {
        log.info("Creating new category: {}", categoryDto);

        categoryRepository.findByNameContainingIgnoreCase(categoryDto.getName())
                .ifPresent(category -> {
                    throw ConflictResource.ofValue(
                            "Категория '" + categoryDto.getName() + "' уже существует",
                            categoryDto.getName()
                    );
                });

        Category category = CategoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with id: {}", savedCategory.getId());

        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        log.info("Updating category with id: {}, data: {}", categoryDto.getId(), categoryDto);

        Category existingCategory = getCategoryById(categoryDto.getId());

        categoryRepository.findByNameContainingIgnoreCaseAndIdNotIn(
                        categoryDto.getName(), List.of(categoryDto.getId()))
                .ifPresent(category -> {
                    throw ConflictResource.ofValue(
                            "Категория '" + categoryDto.getName() + "' уже существует",
                            categoryDto.getName()
                    );
                });

        Category updatedCategory = CategoryMapper.updateEntity(categoryDto, existingCategory);
        updatedCategory = categoryRepository.save(updatedCategory);
        log.info("Updated category with id: {}", updatedCategory.getId());

        return CategoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(long catId) {
        log.info("Deleting category with id: {}", catId);

        Category category = getCategoryById(catId);

        // Проверка через Feign-клиент, есть ли события с этой категорией
        boolean hasEvents = eventClient.existsByCategoryId(catId);
        if (hasEvents) {
            throw ConflictResource.ofReason(
                    "referenced_by_events",
                    "Нельзя удалить категорию: существуют события, связанные с этой категорией"
            );
        }

        categoryRepository.deleteById(catId);
        log.info("Deleted category with id: {}", catId);
    }
}