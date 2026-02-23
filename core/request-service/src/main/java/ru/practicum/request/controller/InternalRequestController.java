package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Map;

/**
 * Внутренний контроллер для межсервисного взаимодействия.
 * Доступен только для других микросервисов.
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class InternalRequestController {

    private final RequestService requestService;

    /**
     * Получает запросы по идентификатору события.
     */
    @GetMapping("/by-event/{eventId}")
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable long eventId) {
        return requestService.getRequestsByEventId(eventId);
    }

    /**
     * Получает запросы по списку идентификаторов.
     */
    @GetMapping("/by-ids")
    public List<ParticipationRequestDto> getRequestsByIds(@RequestParam List<Long> ids) {
        return requestService.getRequestsByIds(ids);
    }

    /**
     * Получает количество подтвержденных запросов для события.
     */
    @GetMapping("/count-confirmed/{eventId}")
    public Long countConfirmedRequestsByEventId(@PathVariable long eventId) {
        return requestService.countConfirmedRequestsByEventId(eventId);
    }

    /**
     * Получает количество подтвержденных запросов для списка событий.
     */
    @GetMapping("/count-confirmed-by-events")
    public Map<Long, Long> countConfirmedRequestsByEventIds(@RequestParam List<Long> eventIds) {
        return requestService.countConfirmedRequestsByEventIds(eventIds);
    }

    /**
     * Обновляет статус запроса.
     */
    @PutMapping("/{requestId}/status")
    public ParticipationRequestDto updateRequestStatus(@PathVariable long requestId,
                                                       @RequestParam Status status) {
        return requestService.updateRequestStatus(requestId, status);
    }
}