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
import java.util.Optional;

/**
 * Реализация сервиса для работы с категориями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventClient eventClient;

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        log.info("Запрос всех категорий: from={}, size={}", from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto get(long catId) {
        log.info("Запрос категории по id: {}", catId);
        Category category = getCategoryEntityById(catId);
        return categoryMapper.toDto(category);
    }

    @Override
    public Category getCategoryById(long catId) {
        return getCategoryEntityById(catId);
    }

    private Category getCategoryEntityById(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundResource("Категория", catId));
    }

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto categoryDto) {
        log.info("Создание новой категории: {}", categoryDto);

        checkNameUnique(categoryDto.getName(), null);

        Category category = categoryMapper.toEntity(categoryDto);
        Category saved = categoryRepository.save(category);
        log.info("Создана категория с id: {}", saved.getId());

        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        log.info("Обновление категории id={}, данные: {}", categoryDto.getId(), categoryDto);

        Category existing = getCategoryEntityById(categoryDto.getId());
        checkNameUnique(categoryDto.getName(), categoryDto.getId());

        // Преобразуем CategoryDto в NewCategoryDto для маппера обновления
        NewCategoryDto newCategoryDto = new NewCategoryDto(categoryDto.getName());
        categoryMapper.updateEntityFromDto(newCategoryDto, existing);

        Category updated = categoryRepository.save(existing);
        log.info("Обновлена категория с id: {}", updated.getId());

        return categoryMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(long catId) {
        log.info("Удаление категории id={}", catId);

        Category category = getCategoryEntityById(catId);

        boolean hasEvents = eventClient.existsByCategoryId(catId);
        if (hasEvents) {
            throw ConflictResource.ofReason(
                    "referenced_by_events",
                    "Нельзя удалить категорию: существуют события, связанные с этой категорией"
            );
        }

        categoryRepository.deleteById(catId);
        log.info("Удалена категория с id: {}", catId);
    }

    /**
     * Проверяет уникальность имени категории.
     *
     * @param name       проверяемое имя
     * @param excludedId идентификатор категории, исключаемый из проверки (при обновлении)
     * @throws ConflictResource если имя уже занято
     */
    private void checkNameUnique(String name, Long excludedId) {
        Optional<Category> existing = (excludedId == null)
                ? categoryRepository.findByNameIgnoreCase(name)
                : categoryRepository.findByNameIgnoreCaseAndIdNot(name, excludedId);

        existing.ifPresent(c -> {
            throw ConflictResource.ofValue(
                    "Категория с именем '" + name + "' уже существует",
                    name
            );
        });
    }
}