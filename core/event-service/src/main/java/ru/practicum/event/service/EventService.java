package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.util.EventGetAdminParam;
import ru.practicum.event.util.EventGetPublicParam;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

/**
 * Сервис для работы с событиями.
 */
public interface EventService {

    /**
     * Получает запросы на участие в событии.
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
     * Получает события для администратора.
     *
     * @param param параметры фильтрации
     * @return список событий
     */
    List<EventFullDto> getEventsByAdmin(EventGetAdminParam param);

    /**
     * Обновляет событие администратором.
     *
     * @param eventId     идентификатор события
     * @param updateEvent данные для обновления
     * @return обновленное событие
     */
    EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest updateEvent);

    /**
     * Получает события для публичного доступа.
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
     * Получает сущность события.
     *
     * @param eventId идентификатор события
     * @return сущность события
     */
    Event getEventById(long eventId);

    // Новые методы для Feign-клиентов

    /**
     * Получает краткую информацию о событии по его идентификатору.
     *
     * @param eventId идентификатор события
     * @return краткое DTO события
     */
    EventShortDto getEventShortById(Long eventId);

    /**
     * Получает список событий по их идентификаторам.
     *
     * @param ids список идентификаторов событий
     * @return список кратких DTO событий
     */
    List<EventShortDto> getEventsByIds(List<Long> ids);

    /**
     * Проверяет существование события по его идентификатору.
     *
     * @param eventId идентификатор события
     * @return true если событие существует
     */
    Boolean existsEventById(Long eventId);

    /**
     * Проверяет, существуют ли события с указанной категорией.
     *
     * @param categoryId идентификатор категории
     * @return true если есть события с данной категорией
     */
    Boolean existsByCategoryId(Long categoryId);
}