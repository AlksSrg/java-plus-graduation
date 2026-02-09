package ru.practicum.event.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.event.utill.EventGetAdminParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Контроллер для административных операций с событиями.
 */
@Validated
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;

    /**
     * Получает список событий с фильтрацией для администратора.
     *
     * @param users      список идентификаторов инициаторов
     * @param states     список статусов событий
     * @param categories список идентификаторов категорий
     * @param rangeStart начало временного интервала
     * @param rangeEnd   конец временного интервала
     * @param from       начальная позиция
     * @param size       количество элементов
     * @return список событий
     */
    @GetMapping
    public List<EventFullDto> getEventsForAdmin(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<String> states,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
                                                @RequestParam(required = false)
                                                @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        EventGetAdminParam param = EventGetAdminParam.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        return eventService.getEventsByAdmin(param);
    }

    /**
     * Обновляет событие администратором.
     *
     * @param eventId     идентификатор события
     * @param updateEvent данные для обновления
     * @return обновленное событие
     */
    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable @Positive Long eventId,
                                           @RequestBody @Valid UpdateEventAdminRequest updateEvent) {
        return eventService.updateEventByAdmin(eventId, updateEvent);
    }
}