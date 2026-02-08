package ru.practicum.compilation.controller.public_;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

/**
 * Публичный контроллер для работы с подборками событий.
 */
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CompilationPublicController {

    private final CompilationService compilationService;

    /**
     * Получает список подборок событий с возможностью фильтрации и пагинации.
     *
     * @param pinned фильтр по закрепленным подборкам
     * @param from   начальная позиция в списке
     * @param size   количество элементов на странице
     * @return список подборок событий
     */
    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Getting compilations with parameters: pinned={}, from={}, size={}", pinned, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        return compilationService.getCompilations(pinned, pageable);
    }

    /**
     * Получает подборку событий по идентификатору.
     *
     * @param compId идентификатор подборки
     * @return подборка событий
     */
    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("Getting compilation by id: {}", compId);
        return compilationService.getCompilationById(compId);
    }
}