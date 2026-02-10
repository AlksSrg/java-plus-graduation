package ru.practicum.event.controller.public_;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;
import ru.practicum.event.utill.EventGetPublicParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Публичный контроллер для операций с событиями.
 */
@Validated
@RequestMapping("/events")
@RestController
@RequiredArgsConstructor
public class EventPublicController {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String APPLICATION = "main-service";
    private static final String EVENT_URI_PATTERN = "/events/%d";
    private final EventService eventService;
    private final StatsClient statsClient;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Получает список событий с фильтрацией для публичного доступа.
     *
     * @param text          текст для поиска в аннотации и описании
     * @param categories    список идентификаторов категорий
     * @param paid          фильтр по платности события
     * @param rangeStart    начало временного интервала
     * @param rangeEnd      конец временного интервала
     * @param onlyAvailable только события с доступными местами
     * @param sort          способ сортировки
     * @param from          начальная позиция
     * @param size          количество элементов
     * @param request       HTTP запрос
     * @return список событий
     */
    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            HttpServletRequest request) {

        EventGetPublicParam param = EventGetPublicParam.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart == null && rangeEnd == null ? LocalDateTime.now() : rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        List<EventShortDto> events = eventService.getEventsByPublic(param);
        statsClient.saveStat(APPLICATION, request.getRequestURI(), request.getRemoteAddr());
        return events;
    }

    /**
     * Получает событие по идентификатору для публичного доступа.
     *
     * @param id      идентификатор события
     * @param request HTTP запрос
     * @return событие
     */
    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable @Positive long id,
                                 HttpServletRequest request) {
        EventFullDto eventFullDto = eventService.getEventByPublic(id);
        executorService.execute(() ->
                statsClient.saveStat(APPLICATION, request.getRequestURI(), request.getRemoteAddr()));
        return eventFullDto;
    }
}