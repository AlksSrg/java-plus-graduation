package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.config.FeignConfiguration;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с compilation-service.
 * Предоставляет методы для получения подборок событий.
 */
@FeignClient(name = "compilation-service", path = "/compilations", configuration = FeignConfiguration.class)
public interface CompilationClient {

    /**
     * Получает список подборок с возможностью фильтрации по признаку закрепления.
     *
     * @param pinned признак закреплённости (true/false)
     * @param from   смещение для пагинации
     * @param size   количество элементов на странице
     * @return список подборок
     */
    @GetMapping
    List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size);

    /**
     * Получает подборку по её идентификатору.
     *
     * @param compId идентификатор подборки
     * @return DTO подборки
     */
    @GetMapping("/{compId}")
    CompilationDto getCompilationById(@PathVariable("compId") Long compId);
}