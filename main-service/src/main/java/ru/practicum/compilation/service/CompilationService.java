package ru.practicum.compilation.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

/**
 * Сервис для работы с подборками событий.
 */
public interface CompilationService {

    /**
     * Создает новую подборку событий.
     *
     * @param newCompilationDto данные для создания
     * @return созданная подборка
     */
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    /**
     * Удаляет подборку событий.
     *
     * @param compId идентификатор подборки
     */
    void deleteCompilation(Long compId);

    /**
     * Обновляет существующую подборку событий.
     *
     * @param compId        идентификатор подборки
     * @param updateRequest данные для обновления
     * @return обновленная подборка
     */
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    /**
     * Получает список подборок с фильтрацией.
     *
     * @param pinned   фильтр по закрепленности
     * @param pageable параметры пагинации
     * @return список подборок
     */
    List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable);

    /**
     * Получает подборку по идентификатору.
     *
     * @param compId идентификатор подборки
     * @return подборка событий
     */
    CompilationDto getCompilationById(Long compId);
}