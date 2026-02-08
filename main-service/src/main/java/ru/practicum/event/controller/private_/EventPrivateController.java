package ru.practicum.event.controller.private_;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

/**
 * Контроллер для приватных операций с событиями пользователей.
 */
@Validated
@RequestMapping("/users/{userId}/events")
@RestController
@RequiredArgsConstructor
public class EventPrivateController {
    private final EventService eventService;

    /**
     * Получает запросы на участие в событии пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return список запросов на участие
     */
    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive long userId,
                                                     @PathVariable @Positive long eventId) {
        return eventService.getRequests(userId, eventId);
    }

    /**
     * Получает событие пользователя по идентификатору.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return событие
     */
    @GetMapping("/{eventId}")
    public EventFullDto get(@PathVariable @Positive long userId,
                            @PathVariable @Positive long eventId) {
        return eventService.get(userId, eventId);
    }

    /**
     * Получает все события пользователя.
     *
     * @param userId идентификатор пользователя
     * @param from   начальная позиция
     * @param size   количество элементов
     * @return список событий
     */
    @GetMapping
    public List<EventShortDto> getAll(@PathVariable @Positive long userId,
                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                      @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getAll(userId, from, size);
    }

    /**
     * Создает новое событие.
     *
     * @param userId   идентификатор пользователя
     * @param eventDto данные события
     * @return созданное событие
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto create(@PathVariable @Positive long userId,
                        @RequestBody @Valid NewEventDto eventDto) {
        return eventService.create(userId, eventDto);
    }

    /**
     * Обновляет событие пользователя.
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор события
     * @param updateEvent данные для обновления
     * @return обновленное событие
     */
    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable @Positive long userId,
                               @PathVariable @Positive long eventId,
                               @RequestBody @Valid UpdateEventUserRequest updateEvent) {
        return eventService.update(userId, eventId, updateEvent);
    }

    /**
     * Обновляет статусы запросов на участие в событии.
     *
     * @param userId             идентификатор пользователя
     * @param eventId            идентификатор события
     * @param eventRequestStatus данные для обновления статусов
     * @return результат обновления статусов
     */
    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable @Positive long userId,
                                                              @PathVariable @Positive long eventId,
                                                              @RequestBody @Valid
                                                              EventRequestStatusUpdateRequest eventRequestStatus) {
        return eventService.updateRequestStatus(userId, eventId, eventRequestStatus);
    }
}