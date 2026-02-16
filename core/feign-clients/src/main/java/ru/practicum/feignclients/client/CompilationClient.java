package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.compilation.dto.CompilationDto;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с микросервисом подборок (compilation-service).
 * Предоставляет методы для публичного API, так как подборки доступны для всех.
 */
@FeignClient(name = "compilation-service", path = "/compilations")
public interface CompilationClient {

    /**
     * Получить список подборок с возможностью фильтрации и пагинации.
     *
     * @param pinned фильтр по закрепленным подборкам
     * @param from   начальная позиция в списке
     * @param size   количество элементов на странице
     * @return список подборок событий
     */
    @GetMapping
    List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size);

    /**
     * Получить подборку событий по идентификатору.
     *
     * @param compId идентификатор подборки
     * @return подборка событий
     */
    @GetMapping("/{compId}")
    CompilationDto getCompilationById(@PathVariable("compId") Long compId);
}