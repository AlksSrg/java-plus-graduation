package ru.practicum.compilation.service;

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
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с подборками событий.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating new compilation with title: {}", newCompilationDto.getTitle());

        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new ConflictResource("Compilation with title '" + newCompilationDto.getTitle() + "' already exists");
        }

        Compilation compilation = CompilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            compilation.setEvents(new HashSet<>(events));
        } else {
            compilation.setEvents(new HashSet<>());
        }

        try {
            Compilation savedCompilation = compilationRepository.save(compilation);
            log.info("Compilation created successfully with id: {}", savedCompilation.getId());
            return CompilationMapper.toDto(savedCompilation);
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

        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));
            compilation.setEvents(events);
        }

        try {
            Compilation updatedCompilation = compilationRepository.save(compilation);
            log.info("Compilation with id: {} updated successfully", compId);
            return CompilationMapper.toDto(updatedCompilation);
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

        return compilations.stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation by id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundResource("Compilation with id=" + compId + " was not found"));

        return CompilationMapper.toDto(compilation);
    }
}