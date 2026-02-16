package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с event-service.
 * Предоставляет методы для получения информации о событиях.
 */
@FeignClient(name = "event-service", path = "/events")
public interface EventClient {

    /**
     * Получает полную информацию о событии по его идентификатору.
     *
     * @param eventId идентификатор события
     * @return полное DTO события
     */
    @GetMapping("/{eventId}")
    EventFullDto getEventById(@PathVariable("eventId") Long eventId);

    /**
     * Получает краткую информацию о событии по его идентификатору.
     * Используется для отображения в списках и комментариях.
     *
     * @param eventId идентификатор события
     * @return краткое DTO события
     */
    @GetMapping("/{eventId}/short")
    EventShortDto getEventShortById(@PathVariable("eventId") Long eventId);

    /**
     * Получает список событий по их идентификаторам.
     *
     * @param eventIds список идентификаторов событий
     * @return список кратких DTO событий
     */
    @GetMapping("/by-ids")
    List<EventShortDto> getEventsByIds(@RequestParam("ids") List<Long> eventIds);

    /**
     * Проверяет существование события по его идентификатору.
     *
     * @param eventId идентификатор события
     * @return true если событие существует
     */
    @GetMapping("/{eventId}/exists")
    Boolean existsEventById(@PathVariable("eventId") Long eventId);

    /**
     * Проверяет, существуют ли события с указанной категорией.
     *
     * @param categoryId идентификатор категории
     * @return true если есть события с данной категорией
     */
    @GetMapping("/by-category/{categoryId}/exists")
    Boolean existsByCategoryId(@PathVariable("categoryId") Long categoryId);
}