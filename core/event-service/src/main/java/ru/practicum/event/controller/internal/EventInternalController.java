package ru.practicum.event.controller.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.util.List;

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

    /**
     * Получает краткую информацию о событии по его идентификатору для внутренних вызовов.
     *
     * @param eventId идентификатор события
     * @return краткая информация о событии
     */
    @GetMapping("/{eventId}/short")
    public EventShortDto getEventShortById(@PathVariable Long eventId) {
        log.info("Internal call: GET /events/internal/{}/short", eventId);
        return eventService.getEventShortById(eventId);
    }

    /**
     * Получает список событий по их идентификаторам для внутренних вызовов.
     *
     * @param ids список идентификаторов событий
     * @return список краткой информации о событиях
     */
    @GetMapping("/by-ids")
    public List<EventShortDto> getEventsByIds(@RequestParam List<Long> ids) {
        log.info("Internal call: GET /events/internal/by-ids with ids: {}", ids);
        return eventService.getEventsByIds(ids);
    }

    /**
     * Проверяет существование события по его идентификатору для внутренних вызовов.
     *
     * @param eventId идентификатор события
     * @return true если событие существует
     */
    @GetMapping("/{eventId}/exists")
    public Boolean existsEventById(@PathVariable Long eventId) {
        log.info("Internal call: GET /events/internal/{}/exists", eventId);
        return eventService.existsEventById(eventId);
    }

    /**
     * Проверяет, существуют ли события с указанной категорией для внутренних вызовов.
     *
     * @param categoryId идентификатор категории
     * @return true если есть события с данной категорией
     */
    @GetMapping("/by-category/{categoryId}/exists")
    public Boolean existsByCategoryId(@PathVariable Long categoryId) {
        log.info("Internal call: GET /events/internal/by-category/{}/exists", categoryId);
        return eventService.existsByCategoryId(categoryId);
    }
}