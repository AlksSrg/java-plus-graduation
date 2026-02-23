package ru.practicum.event.controller.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.service.EventService;

/**
 * Внутренний контроллер для межсервисного взаимодействия.
 * Предоставляет API для других микросервизов без проверки статуса PUBLISHED.
 */
@Slf4j
@RestController
@RequestMapping("/events/internal")
@RequiredArgsConstructor
public class EventInternalController {

    private final EventService eventService;

    /**
     * Получает событие по идентификатору для внутренних вызовов.
     * В отличие от публичного эндпоинта, этот метод не проверяет статус PUBLISHED,
     * что позволяет другим микросервисам получать события на любом этапе жизненного цикла.
     *
     * @param eventId идентификатор события
     * @return полное DTO события
     */
    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable long eventId) {
        log.info("Internal call: GET /events/internal/{}", eventId);
        return eventService.getEventByIdInternal(eventId);
    }
}