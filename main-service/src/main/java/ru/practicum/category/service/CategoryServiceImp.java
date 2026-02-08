package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;

import java.util.List;

/**
 * Реализация сервиса для работы с категориями.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImp implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryDto::mapFromCategory)
                .toList();
    }

    @Override
    public CategoryDto get(long catId) {
        Category category = getCategoryById(catId);
        return CategoryDto.mapFromCategory(category);
    }

    @Override
    public Category getCategoryById(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundResource("Категория с id=" + catId + " не найдена"));
    }

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto categoryDto) {
        categoryRepository.findByNameContainingIgnoreCase(categoryDto.getName())
                .ifPresent(category -> {
                    throw new ConflictResource("Категория '" + categoryDto.getName() + "' уже существует");
                });

        Category category = categoryDto.mapToCategory();
        Category savedCategory = categoryRepository.save(category);

        return CategoryDto.mapFromCategory(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        Category existingCategory = getCategoryById(categoryDto.getId());

        categoryRepository.findByNameContainingIgnoreCaseAndIdNotIn(categoryDto.getName(),
                        List.of(categoryDto.getId()))
                .ifPresent(category -> {
                    throw new ConflictResource("Категория '" + categoryDto.getName() + "' уже существует");
                });

        existingCategory.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(existingCategory);

        return CategoryDto.mapFromCategory(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(long catId) {
        Category category = getCategoryById(catId);

        boolean hasEvents = eventRepository.existsByCategoryId(catId);
        if (hasEvents) {
            throw new ConflictResource("Нельзя удалить категорию: существуют события, связанные с этой категорией");
        }

        categoryRepository.deleteById(catId);
    }
}