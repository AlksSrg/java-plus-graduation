package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Map;

/**
 * Feign-клиент для взаимодействия с Request Service.
 */
@FeignClient(name = "request-service", path = "/requests")
public interface RequestClient {

    /**
     * Получает запросы по идентификатору события.
     *
     * @param eventId идентификатор события
     * @return список запросов
     */
    @GetMapping("/by-event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable("eventId") long eventId);

    /**
     * Получает запросы по списку идентификаторов.
     *
     * @param ids список идентификаторов запросов
     * @return список запросов
     */
    @GetMapping("/by-ids")
    List<ParticipationRequestDto> getRequestsByIds(@RequestParam("ids") List<Long> ids);

    /**
     * Получает количество подтвержденных запросов для события.
     *
     * @param eventId идентификатор события
     * @return количество подтвержденных запросов
     */
    @GetMapping("/count-confirmed/{eventId}")
    Long countConfirmedRequestsByEventId(@PathVariable("eventId") long eventId);

    /**
     * Получает количество подтвержденных запросов для списка событий.
     *
     * @param eventIds список идентификаторов событий
     * @return карта eventId -> количество подтвержденных запросов
     */
    @GetMapping("/count-confirmed-by-events")
    Map<Long, Long> countConfirmedRequestsByEventIds(@RequestParam("eventIds") List<Long> eventIds);

    /**
     * Обновляет статус запроса.
     *
     * @param requestId идентификатор запроса
     * @param status    новый статус
     * @return обновленный запрос
     */
    @PutMapping("/{requestId}/status")
    ParticipationRequestDto updateRequestStatus(@PathVariable("requestId") long requestId,
                                                @RequestParam("status") Status status);
}