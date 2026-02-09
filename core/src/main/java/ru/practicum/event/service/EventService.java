package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.utill.EventGetAdminParam;
import ru.practicum.event.utill.EventGetPublicParam;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

/**
 * Сервис для работы с событиями.
 */
public interface EventService {

    /**
     * Получает запросы на участие в событии пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return список запросов
     */
    List<ParticipationRequestDto> getRequests(long userId, long eventId);

    /**
     * Получает событие пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return событие
     */
    EventFullDto get(long userId, long eventId);

    /**
     * Получает все события пользователя.
     *
     * @param userId идентификатор пользователя
     * @param from   начальная позиция
     * @param size   количество элементов
     * @return список событий
     */
    List<EventShortDto> getAll(long userId, int from, int size);

    /**
     * Создает новое событие.
     *
     * @param userId   идентификатор пользователя
     * @param eventDto данные события
     * @return созданное событие
     */
    EventFullDto create(long userId, NewEventDto eventDto);

    /**
     * Обновляет событие пользователя.
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор события
     * @param updateEvent данные для обновления
     * @return обновленное событие
     */
    EventFullDto update(long userId, long eventId, UpdateEventUserRequest updateEvent);

    /**
     * Обновляет статусы запросов на участие.
     *
     * @param userId             идентификатор пользователя
     * @param eventId            идентификатор события
     * @param eventRequestStatus данные для обновления
     * @return результат обновления
     */
    EventRequestStatusUpdateResult updateRequestStatus(long userId, long eventId,
                                                       EventRequestStatusUpdateRequest eventRequestStatus);

    /**
     * Получает события для администратора с фильтрацией.
     *
     * @param param параметры фильтрации
     * @return список событий
     */
    List<EventFullDto> getEventsByAdmin(EventGetAdminParam param);

    /**
     * Обновляет событие администратором.
     *
     * @param eventId                 идентификатор события
     * @param updateEventAdminRequest запрос на обновление
     * @return обновленное событие
     */
    EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    /**
     * Получает события для публичного доступа с фильтрацией.
     *
     * @param param параметры фильтрации
     * @return список событий
     */
    List<EventShortDto> getEventsByPublic(EventGetPublicParam param);

    /**
     * Получает событие для публичного доступа.
     *
     * @param eventId идентификатор события
     * @return событие
     */
    EventFullDto getEventByPublic(long eventId);

    /**
     * Получает событие.
     *
     * @param eventId идентификатор события
     * @return событие
     */
    Event getEventById(long eventId);
}