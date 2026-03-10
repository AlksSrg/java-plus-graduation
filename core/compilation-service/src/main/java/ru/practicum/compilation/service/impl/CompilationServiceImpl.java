package ru.practicum.compilation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.feignclients.client.EventClient;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с подборками событий.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating new compilation with title: {}", newCompilationDto.getTitle());

        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new ConflictResource("Compilation with title '" + newCompilationDto.getTitle() + "' already exists");
        }

        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        // Дополнительная защита от null (если маппер по какой-то причине не сработает)
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }

        // Проверяем существование событий через EventClient
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Long> eventIds = newCompilationDto.getEvents();
            List<EventShortDto> events = eventClient.getEventsByIds(eventIds);

            if (events.size() != eventIds.size()) {
                Set<Long> foundIds = events.stream().map(EventShortDto::getId).collect(Collectors.toSet());
                Set<Long> notFoundIds = new HashSet<>(eventIds);
                notFoundIds.removeAll(foundIds);
                throw new NotFoundResource("Events with ids=" + notFoundIds + " were not found");
            }

            compilation.setEventIds(eventIds);
        } else {
            compilation.setEventIds(new ArrayList<>());
        }

        try {
            Compilation savedCompilation = compilationRepository.save(compilation);
            log.info("Compilation created successfully with id: {}, pinned: {}",
                    savedCompilation.getId(), savedCompilation.getPinned());

            List<EventShortDto> events = savedCompilation.getEventIds().isEmpty()
                    ? Collections.emptyList()
                    : eventClient.getEventsByIds(new ArrayList<>(savedCompilation.getEventIds()));

            return compilationMapper.toDto(savedCompilation, events);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictResource("Compilation creation failed due to data integrity violation");
        }
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundResource("Compilation with id=" + compId + " was not found");
        }

        compilationRepository.deleteById(compId);
        log.info("Compilation with id: {} deleted successfully", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id: {}", compId);

        Compilation compilation = getCompilationOrThrow(compId);

        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            if (!compilation.getTitle().equals(updateRequest.getTitle()) &&
                    compilationRepository.existsByTitle(updateRequest.getTitle())) {
                throw new ConflictResource("Compilation with title '" + updateRequest.getTitle() + "' already exists");
            }
        }

        // Обновляем список событий через EventClient
        if (updateRequest.getEvents() != null) {
            if (updateRequest.getEvents().isEmpty()) {
                compilation.setEventIds(new ArrayList<>());
            } else {
                List<EventShortDto> events = eventClient.getEventsByIds(updateRequest.getEvents());
                if (events.size() != updateRequest.getEvents().size()) {
                    Set<Long> foundIds = events.stream().map(EventShortDto::getId).collect(Collectors.toSet());
                    Set<Long> notFoundIds = new HashSet<>(updateRequest.getEvents());
                    notFoundIds.removeAll(foundIds);
                    throw new NotFoundResource("Events with ids=" + notFoundIds + " were not found");
                }
                compilation.setEventIds(updateRequest.getEvents());
            }
        }

        compilationMapper.updateEntityFromRequest(updateRequest, compilation);

        try {
            Compilation updatedCompilation = compilationRepository.save(compilation);
            log.info("Compilation with id: {} updated successfully", compId);

            List<EventShortDto> events = updatedCompilation.getEventIds().isEmpty()
                    ? Collections.emptyList()
                    : eventClient.getEventsByIds(new ArrayList<>(updatedCompilation.getEventIds()));

            return compilationMapper.toDto(updatedCompilation, events);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictResource("Compilation update failed due to data integrity violation");
        }
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        log.info("Getting compilations with pinned={}, pageable={}", pinned, pageable);

        // Сортировка по id для стабильной пагинации
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("id").ascending()
        );

        List<Long> ids = compilationRepository.findIdsByPinned(pinned, sortedPageable);

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Compilation> compilations = compilationRepository.findAllByIdIn(ids);

        // Восстанавливаем порядок согласно отсортированным ID
        Map<Long, Compilation> compilationMap = compilations.stream()
                .collect(Collectors.toMap(Compilation::getId, Function.identity()));
        List<Compilation> orderedCompilations = ids.stream()
                .map(compilationMap::get)
                .filter(Objects::nonNull)
                .toList();

        // Собираем все ID событий из всех подборок для одного запроса
        Set<Long> allEventIds = orderedCompilations.stream()
                .flatMap(c -> c.getEventIds().stream())
                .collect(Collectors.toSet());

        Map<Long, EventShortDto> eventsMap;
        if (!allEventIds.isEmpty()) {
            List<EventShortDto> events = eventClient.getEventsByIds(new ArrayList<>(allEventIds));
            eventsMap = events.stream().collect(Collectors.toMap(EventShortDto::getId, Function.identity()));
        } else {
            eventsMap = new HashMap<>();
        }

        return orderedCompilations.stream()
                .map(compilation -> {
                    List<EventShortDto> compilationEvents = compilation.getEventIds().stream()
                            .map(eventsMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                    return compilationMapper.toDto(compilation, compilationEvents);
                })
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation by id: {}", compId);

        Compilation compilation = getCompilationOrThrow(compId);

        List<EventShortDto> events = compilation.getEventIds().isEmpty()
                ? Collections.emptyList()
                : eventClient.getEventsByIds(new ArrayList<>(compilation.getEventIds()));

        return compilationMapper.toDto(compilation, events);
    }

    private Compilation getCompilationOrThrow(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundResource("Compilation with id=" + compId + " was not found"));
    }
}