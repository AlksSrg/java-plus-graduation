package ru.practicum.compilation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.feignclients.client.EventClient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с подборками событий.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements ru.practicum.compilation.service.CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating new compilation with title: {}", newCompilationDto.getTitle());

        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new ConflictResource("Compilation with title '" + newCompilationDto.getTitle() + "' already exists");
        }

        Compilation compilation = CompilationMapper.toEntity(newCompilationDto);

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

            compilation.setEventIds(new HashSet<>(eventIds));
        } else {
            compilation.setEventIds(new HashSet<>());
        }

        try {
            Compilation savedCompilation = compilationRepository.save(compilation);
            log.info("Compilation created successfully with id: {}", savedCompilation.getId());

            // Получаем полные данные о событиях для ответа
            List<EventShortDto> events = savedCompilation.getEventIds().isEmpty()
                    ? Collections.emptyList()
                    : eventClient.getEventsByIds(new ArrayList<>(savedCompilation.getEventIds()));

            return CompilationMapper.toDto(savedCompilation, events);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictResource("Compilation creation failed due to data integrity violation");
        }
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundResource("Compilation with id=" + compId + " was not found"));

        compilationRepository.delete(compilation);
        log.info("Compilation with id: {} deleted successfully", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundResource("Compilation with id=" + compId + " was not found"));

        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            if (!compilation.getTitle().equals(updateRequest.getTitle()) &&
                    compilationRepository.existsByTitle(updateRequest.getTitle())) {
                throw new ConflictResource("Compilation with title '" + updateRequest.getTitle() + "' already exists");
            }
            compilation.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        // Обновляем список событий через EventClient
        if (updateRequest.getEvents() != null) {
            if (!updateRequest.getEvents().isEmpty()) {
                List<EventShortDto> events = eventClient.getEventsByIds(updateRequest.getEvents());
                if (events.size() != updateRequest.getEvents().size()) {
                    Set<Long> foundIds = events.stream().map(EventShortDto::getId).collect(Collectors.toSet());
                    Set<Long> notFoundIds = new HashSet<>(updateRequest.getEvents());
                    notFoundIds.removeAll(foundIds);
                    throw new NotFoundResource("Events with ids=" + notFoundIds + " were not found");
                }
            }
            compilation.setEventIds(new HashSet<>(updateRequest.getEvents()));
        }

        try {
            Compilation updatedCompilation = compilationRepository.save(compilation);
            log.info("Compilation with id: {} updated successfully", compId);

            // Получаем полные данные о событиях для ответа
            List<EventShortDto> events = updatedCompilation.getEventIds().isEmpty()
                    ? Collections.emptyList()
                    : eventClient.getEventsByIds(new ArrayList<>(updatedCompilation.getEventIds()));

            return CompilationMapper.toDto(updatedCompilation, events);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictResource("Compilation update failed due to data integrity violation");
        }
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        log.info("Getting compilations with pinned={}, pageable={}", pinned, pageable);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        // Собираем все ID событий из всех подборок
        Set<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEventIds().stream())
                .collect(Collectors.toSet());

        // Получаем все события одним запросом (решаем проблему N+1)
        Map<Long, EventShortDto> eventsMap;
        if (!allEventIds.isEmpty()) {
            List<EventShortDto> events = eventClient.getEventsByIds(new ArrayList<>(allEventIds));
            eventsMap = events.stream().collect(Collectors.toMap(EventShortDto::getId, e -> e));
        } else {
            eventsMap = new HashMap<>();
        }

        // Маппим каждую подборку с ее событиями
        return compilations.stream()
                .map(compilation -> {
                    List<EventShortDto> compilationEvents = compilation.getEventIds().stream()
                            .map(eventsMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return CompilationMapper.toDto(compilation, compilationEvents);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation by id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundResource("Compilation with id=" + compId + " was not found"));

        // Получаем данные о событиях через EventClient
        List<EventShortDto> events = compilation.getEventIds().isEmpty()
                ? Collections.emptyList()
                : eventClient.getEventsByIds(new ArrayList<>(compilation.getEventIds()));

        return CompilationMapper.toDto(compilation, events);
    }
}