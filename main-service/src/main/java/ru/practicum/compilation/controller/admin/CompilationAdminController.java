package ru.practicum.compilation.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

/**
 * Контроллер для административных операций с подборками событий.
 */
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;

    /**
     * Создает новую подборку событий.
     *
     * @param newCompilationDto данные для создания подборки
     * @return созданная подборка
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Creating new compilation with title: {}", newCompilationDto.getTitle());
        return compilationService.createCompilation(newCompilationDto);
    }

    /**
     * Удаляет подборку событий по идентификатору.
     *
     * @param compId идентификатор подборки
     */
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Deleting compilation with id: {}", compId);
        compilationService.deleteCompilation(compId);
    }

    /**
     * Обновляет существующую подборку событий.
     *
     * @param compId        идентификатор подборки
     * @param updateRequest данные для обновления
     * @return обновленная подборка
     */
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id: {}", compId);
        return compilationService.updateCompilation(compId, updateRequest);
    }
}