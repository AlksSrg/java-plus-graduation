package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Map;

/**
 * Внутренний контроллер для межсервисного взаимодействия.
 * Предоставляет API, доступное только для других микросервисов (через Feign-клиенты).
 */
@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class InternalRequestController {

    private final RequestService requestService;

    /**
     * Получает все запросы для указанного события.
     *
     * @param eventId идентификатор события
     * @return список запросов
     */
    @GetMapping("/by-event/{eventId}")
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable long eventId) {
        log.info("Internal: запрос запросов для события {}", eventId);
        return requestService.getRequestsByEventId(eventId);
    }

    /**
     * Получает запросы по списку их идентификаторов.
     *
     * @param ids список идентификаторов запросов
     * @return список запросов
     */
    @GetMapping("/by-ids")
    public List<ParticipationRequestDto> getRequestsByIds(@RequestParam List<Long> ids) {
        log.info("Internal: запрос запросов по ids {}", ids);
        return requestService.getRequestsByIds(ids);
    }

    /**
     * Получает количество подтверждённых запросов для события.
     *
     * @param eventId идентификатор события
     * @return количество подтверждённых запросов
     */
    @GetMapping("/count-confirmed/{eventId}")
    public Long countConfirmedRequestsByEventId(@PathVariable long eventId) {
        log.info("Internal: подсчёт подтверждённых для события {}", eventId);
        return requestService.countConfirmedRequestsByEventId(eventId);
    }

    /**
     * Получает количество подтверждённых запросов для нескольких событий.
     *
     * @param eventIds список идентификаторов событий
     * @return карта, где ключ — идентификатор события, значение — количество подтверждённых запросов
     */
    @GetMapping("/count-confirmed-by-events")
    public Map<Long, Long> countConfirmedRequestsByEventIds(@RequestParam List<Long> eventIds) {
        log.info("Internal: подсчёт подтверждённых для событий {}", eventIds);
        return requestService.countConfirmedRequestsByEventIds(eventIds);
    }

    /**
     * Обновляет статус одного запроса.
     *
     * @param requestId идентификатор запроса
     * @param status    новый статус
     * @return обновлённый запрос
     */
    @PutMapping("/{requestId}/status")
    public ParticipationRequestDto updateRequestStatus(@PathVariable long requestId,
                                                       @RequestParam Status status) {
        log.info("Internal: обновление статуса запроса {} на {}", requestId, status);
        return requestService.updateRequestStatus(requestId, status);
    }

    /**
     * Массовое обновление статусов запросов (используется владельцем события).
     *
     * @param userId  идентификатор владельца события
     * @param eventId идентификатор события
     * @param request данные для обновления (список идентификаторов запросов и новый статус)
     * @return результат обновления: списки подтверждённых и отклонённых запросов
     */
    @PutMapping("/update-requests-status")
    public EventRequestStatusUpdateResult updateRequestsStatus(
            @RequestParam Long userId,
            @RequestParam Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Internal: массовое обновление статусов: userId={}, eventId={}, request={}",
                userId, eventId, request);
        return requestService.updateRequestsStatus(userId, eventId, request);
    }
}