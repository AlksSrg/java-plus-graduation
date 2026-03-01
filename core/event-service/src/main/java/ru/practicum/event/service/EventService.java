package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.util.EventGetAdminParam;
import ru.practicum.event.util.EventGetPublicParam;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventService {

    /**
     * Получает события по заданным параметрам для администратора.
     *
     * @param param параметры фильтрации для администратора
     * @return список событий с полной информацией
     */
    List<EventFullDto> getEventsByAdmin(EventGetAdminParam param);

    /**
     * Обновляет событие администратором.
     *
     * @param eventId       идентификатор события
     * @param updateRequest данные для обновления
     * @return обновленное событие
     */
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    /**
     * Получает события по заданным параметрам для публичного доступа.
     *
     * @param param параметры фильтрации для публичного доступа
     * @return список событий с краткой информацией
     */
    List<EventShortDto> getEventsByPublic(EventGetPublicParam param);

    /**
     * Получает событие по его идентификатору для публичного доступа.
     *
     * @param id идентификатор события
     * @return событие с полной информацией
     */
    EventFullDto getEventByPublic(Long id);

    /**
     * Получает события пользователя.
     *
     * @param userId идентификатор пользователя
     * @param from   количество событий, которые нужно пропустить
     * @param size   количество событий в наборе
     * @return список событий с краткой информацией
     */
    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    /**
     * Создает новое событие.
     *
     * @param userId      идентификатор пользователя
     * @param newEventDto данные нового события
     * @return созданное событие
     */
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    /**
     * Получает событие пользователя по его идентификатору.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return событие с полной информацией
     */
    EventFullDto getEventByUser(Long userId, Long eventId);

    /**
     * Обновляет событие пользователя.
     *
     * @param userId        идентификатор пользователя
     * @param eventId       идентификатор события
     * @param updateRequest данные для обновления
     * @return обновленное событие
     */
    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    /**
     * Получает событие по идентификатору для внутренних вызовов.
     *
     * @param eventId идентификатор события
     * @return полное DTO события
     */
    EventFullDto getEventByIdInternal(Long eventId);

    /**
     * Получает запросы на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return список запросов на участие
     */
    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);

    /**
     * Обновляет статусы запросов на участие в событии.
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор события
     * @param request     данные для обновления статусов
     * @return результат обновления статусов
     */
    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);

    /**
     * Получает список событий по их идентификаторам.
     *
     * @param ids список идентификаторов событий
     * @return список событий с краткой информацией
     */
    List<EventShortDto> getEventsByIds(List<Long> ids);

    /**
     * Получает краткую информацию о событии по его идентификатору.
     *
     * @param eventId идентификатор события
     * @return краткая информация о событии
     */
    EventShortDto getEventShortById(Long eventId);

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

    /**
     * Находит событие по его идентификатору.
     *
     * @param eventId идентификатор события
     * @return событие
     */
    Event findEventById(Long eventId);

    /**
     * Проверяет, участвовал ли пользователь в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return true если пользователь участвовал в событии
     */
    boolean hasUserParticipated(Long userId, Long eventId);
}