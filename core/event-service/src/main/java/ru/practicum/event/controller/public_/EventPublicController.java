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
import ru.practicum.ewm.client.UserActionClient;
import ru.practicum.ewm.client.RecommendationsClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;
import ru.practicum.event.util.EventGetPublicParam;
import ru.practicum.ewm.stats.proto.ActionTypeProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Публичный контроллер для работы с событиями.
 * Предоставляет API для получения информации о событиях без аутентификации.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final EventService eventService;
    private final UserActionClient userActionClient;
    private final RecommendationsClient recommendationsClient;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

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
     * @param request       HTTP-запрос для сбора статистики
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

        List<EventShortDto> events = eventService.getEventsByPublic(param);
        return events;
    }

    /**
     * Получает подробную информацию о событии по его идентификатору.
     *
     * @param id      идентификатор события
     * @param userId  идентификатор пользователя из заголовка
     * @param request HTTP-запрос для сбора статистики
     * @return подробная информация о событии
     */
    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable @Positive long id,
                                 @RequestHeader("X-EWM-USER-ID") Long userId,
                                 HttpServletRequest request) {
        log.info("GET /events/{} by user {}", id, userId);

        EventFullDto eventFullDto = eventService.getEventByPublic(id);

        // Отправляем информацию о просмотре асинхронно
        executorService.submit(() -> {
            try {
                userActionClient.collectUserAction(userId, id, ActionTypeProto.ACTION_VIEW, Instant.now());
                log.debug("Sent VIEW action for user {} event {}", userId, id);
            } catch (Exception e) {
                log.error("Error sending VIEW action to collector for user {} event {}", userId, id, e);
            }
        });

        return eventFullDto;
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

        List<Long> recommendedEventIds = recommendationsClient.getRecommendationsForUser(userId, maxResults)
                .map(proto -> proto.getEventId())
                .collect(Collectors.toList());

        List<EventShortDto> recommendations = eventService.getEventsByIds(recommendedEventIds);

        log.info("Returning {} recommendations for user {}", recommendations.size(), userId);
        return recommendations;
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

        boolean hasParticipated = eventService.hasUserParticipated(userId, eventId);
        if (!hasParticipated) {
            throw new IllegalArgumentException("User can only like events they have participated in");
        }

        executorService.submit(() -> {
            try {
                userActionClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE, Instant.now());
                log.debug("Sent LIKE action for user {} event {}", userId, eventId);
            } catch (Exception e) {
                log.error("Error sending LIKE action to collector for user {} event {}", userId, eventId, e);
            }
        });
    }

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