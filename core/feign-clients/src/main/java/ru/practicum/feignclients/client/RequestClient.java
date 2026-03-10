package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.config.FeignConfiguration;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Map;

/**
 * Feign-клиент для взаимодействия с request-service.
 * Предоставляет методы для работы с запросами на участие в событиях.
 */
@FeignClient(name = "request-service", path = "/requests", configuration = FeignConfiguration.class)
public interface RequestClient {

    /**
     * Получает список запросов для указанного события.
     *
     * @param eventId идентификатор события
     * @return список запросов
     */
    @GetMapping("/by-event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable("eventId") long eventId);

    /**
     * Получает запросы по их идентификаторам.
     *
     * @param ids список идентификаторов запросов
     * @return список запросов
     */
    @GetMapping("/by-ids")
    List<ParticipationRequestDto> getRequestsByIds(@RequestParam("ids") List<Long> ids);

    /**
     * Получает количество подтверждённых запросов для события.
     *
     * @param eventId идентификатор события
     * @return количество подтверждённых запросов
     */
    @GetMapping("/count-confirmed/{eventId}")
    Long countConfirmedRequestsByEventId(@PathVariable("eventId") long eventId);

    /**
     * Получает количество подтверждённых запросов для нескольких событий.
     *
     * @param eventIds список идентификаторов событий
     * @return карта, где ключ — идентификатор события, значение — количество подтверждённых запросов
     */
    @GetMapping("/count-confirmed-by-events")
    Map<Long, Long> countConfirmedRequestsByEventIds(@RequestParam("eventIds") List<Long> eventIds);

    /**
     * Обновляет статус запроса.
     *
     * @param requestId идентификатор запроса
     * @param status    новый статус
     * @return обновлённый запрос
     */
    @PutMapping("/{requestId}/status")
    ParticipationRequestDto updateRequestStatus(@PathVariable("requestId") long requestId,
                                                @RequestParam("status") Status status);
}