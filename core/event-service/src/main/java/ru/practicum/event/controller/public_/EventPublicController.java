package ru.practicum.event.controller.public_;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;
import ru.practicum.event.util.EventGetPublicParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Публичный контроллер для работы с событиями.
 * Предоставляет API для получения информации о событиях без аутентификации,
 * а также для лайков и рекомендаций (с идентификатором пользователя в заголовке).
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final EventService eventService;

    /**
     * Получает список событий с возможностью фильтрации и пагинации.
     *
     * @param text          текст для поиска в аннотации и описании события
     * @param categories    список идентификаторов категорий
     * @param paid          только платные/бесплатные события
     * @param rangeStart    дата и время не раньше которых должно произойти событие
     * @param rangeEnd      дата и время не позже которых должно произойти событие
     * @param onlyAvailable только события с непросроченным лимитом запросов
     * @param sort          способ сортировки: EVENT_DATE или RATING
     * @param from          количество событий, которые нужно пропустить для формирования текущего набора
     * @param size          количество событий в наборе
     * @param request       HTTP-запрос для сбора статистики (используется для логирования)
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

        log.info("GET /events with params: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                        "onlyAvailable={}, sort={}, from={}, size={}", text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);

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

        return eventService.getEventsByPublic(param);
    }

    /**
     * Получает подробную информацию о событии по его идентификатору.
     *
     * @param id      идентификатор события
     * @param userId  идентификатор пользователя из заголовка (может отсутствовать)
     * @param request HTTP-запрос для сбора статистики
     * @return подробная информация о событии
     */
    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable @Positive long id,
                                 @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId,
                                 HttpServletRequest request) {
        log.info("GET /events/{} by user {}", id, userId);
        return eventService.getEventByPublic(id);
    }

    /**
     * Получает рекомендации мероприятий для пользователя.
     *
     * @param userId     идентификатор пользователя из заголовка
     * @param maxResults максимальное количество рекомендаций
     * @return список рекомендуемых событий
     */
    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendations(
            @RequestHeader("X-EWM-USER-ID") Long userId,
            @RequestParam(defaultValue = "10") @Positive int maxResults) {

        log.info("GET /events/recommendations for user {} with maxResults {}", userId, maxResults);
        return eventService.getRecommendations(userId, maxResults);
    }

    /**
     * Ставит лайк мероприятию.
     *
     * @param eventId идентификатор мероприятия
     * @param userId  идентификатор пользователя из заголовка
     */
    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likeEvent(@PathVariable @Positive Long eventId,
                          @RequestHeader("X-EWM-USER-ID") Long userId) {

        log.info("PUT /events/{}/like by user {}", eventId, userId);
        eventService.addLike(userId, eventId);
    }

    // ========== Внутренние эндпоинты для других микросервисов ==========

    /**
     * Получает краткую информацию о событии по его идентификатору для внутренних вызовов.
     *
     * @param eventId идентификатор события
     * @return краткая информация о событии
     */
    @GetMapping("/{eventId}/short")
    public EventShortDto getEventShortById(@PathVariable Long eventId) {
        log.info("Internal call: GET /events/{}/short", eventId);
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
        log.info("Internal call: GET /events/by-ids with ids: {}", ids);
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
        log.info("Internal call: GET /events/{}/exists", eventId);
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
        log.info("Internal call: GET /events/by-category/{}/exists", categoryId);
        return eventService.existsByCategoryId(categoryId);
    }
}