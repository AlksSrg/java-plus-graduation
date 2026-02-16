package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Map;

/**
 * Контроллер для работы с запросами на участие в событиях.
 * Предоставляет API для пользователей и для межсервисного взаимодействия.
 */
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    /**
     * Получает все запросы пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список запросов
     */
    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        return requestService.getRequestsByUserId(userId);
    }

    /**
     * Создает новый запрос на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return созданный запрос
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam Long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    /**
     * Отменяет запрос на участие.
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор запроса
     * @return отмененный запрос
     */
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    // ========== Методы для межсервисного взаимодействия ==========

    /**
     * Получает запросы по идентификатору события.
     *
     * @param eventId идентификатор события
     * @return список запросов
     */
    @GetMapping("/by-event/{eventId}")
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable long eventId) {
        return requestService.getRequestsByEventId(eventId);
    }

    /**
     * Получает запросы по списку идентификаторов.
     *
     * @param ids список идентификаторов запросов
     * @return список запросов
     */
    @GetMapping("/by-ids")
    public List<ParticipationRequestDto> getRequestsByIds(@RequestParam List<Long> ids) {
        return requestService.getRequestsByIds(ids);
    }

    /**
     * Получает количество подтвержденных запросов для события.
     *
     * @param eventId идентификатор события
     * @return количество подтвержденных запросов
     */
    @GetMapping("/count-confirmed/{eventId}")
    public Long countConfirmedRequestsByEventId(@PathVariable long eventId) {
        return requestService.countConfirmedRequestsByEventId(eventId);
    }

    /**
     * Получает количество подтвержденных запросов для списка событий.
     *
     * @param eventIds список идентификаторов событий
     * @return карта eventId -> количество подтвержденных запросов
     */
    @GetMapping("/count-confirmed-by-events")
    public Map<Long, Long> countConfirmedRequestsByEventIds(@RequestParam List<Long> eventIds) {
        return requestService.countConfirmedRequestsByEventIds(eventIds);
    }

    /**
     * Обновляет статус запроса.
     *
     * @param requestId идентификатор запроса
     * @param status    новый статус
     * @return обновленный запрос
     */
    @PutMapping("/{requestId}/status")
    public ParticipationRequestDto updateRequestStatus(@PathVariable long requestId,
                                                       @RequestParam Status status) {
        return requestService.updateRequestStatus(requestId, status);
    }
}