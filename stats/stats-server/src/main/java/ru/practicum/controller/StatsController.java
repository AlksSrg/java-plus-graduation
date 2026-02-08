package ru.practicum.controller;

import constant.DateTimeConstants;
import dto.EndpointHitDto;
import dto.ViewStatsDto;
import dto.ViewStatsRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.EndpointHitService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контроллер для работы со статистикой запросов к эндпоинтам.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final EndpointHitService endpointHitService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateTimeConstants.DATE_TIME_FORMAT_PATTERN);

    /**
     * Сохраняет информацию о запросе к эндпоинту.
     *
     * @param endpointHit данные о запросе
     * @return ответ с результатом операции
     */
    @PostMapping("/hit")
    public ResponseEntity<String> saveHit(@RequestBody EndpointHitDto endpointHit) {
        log.info("POST /hit: {}", endpointHit);

        endpointHitService.addEndpointHit(endpointHit);
        return ResponseEntity.status(HttpStatus.CREATED).body("Информация сохранена");
    }

    /**
     * Получает статистику просмотров за указанный период.
     *
     * @param start  начало периода в строковом формате
     * @param end    конец периода в строковом формате
     * @param uris   список URI для фильтрации
     * @param unique учитывать только уникальные посещения
     * @return список статистики просмотров
     */
    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {

        log.info("GET /stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);

        ViewStatsRequestDto requestDto = ViewStatsRequestDto.builder()
                .start(startDateTime)
                .end(endDateTime)
                .uris(uris)
                .unique(unique)
                .build();

        List<ViewStatsDto> result = endpointHitService.getViewStats(requestDto);
        log.info("Returning {} stats items", result.size());

        return result;
    }
}