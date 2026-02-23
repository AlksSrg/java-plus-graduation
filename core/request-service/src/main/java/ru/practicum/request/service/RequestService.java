package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с запросами на участие.
 */
public interface RequestService {

    /**
     * Получает все запросы пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список запросов
     */
    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    /**
     * Создает новый запрос на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return созданный запрос
     */
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    /**
     * Отменяет запрос на участие.
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор запроса
     * @return отмененный запрос
     */
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    // ========== Методы для межсервисного взаимодействия ==========

    /**
     * Получает запросы по идентификатору события.
     *
     * @param eventId идентификатор события
     * @return список запросов
     */
    List<ParticipationRequestDto> getRequestsByEventId(long eventId);

    /**
     * Получает запросы по списку идентификаторов.
     *
     * @param ids список идентификаторов запросов
     * @return список запросов
     */
    List<ParticipationRequestDto> getRequestsByIds(List<Long> ids);

    /**
     * Получает количество подтвержденных запросов для события.
     *
     * @param eventId идентификатор события
     * @return количество подтвержденных запросов
     */
    Long countConfirmedRequestsByEventId(long eventId);

    /**
     * Получает количество подтвержденных запросов для списка событий.
     *
     * @param eventIds список идентификаторов событий
     * @return карта eventId -> количество подтвержденных запросов
     */
    Map<Long, Long> countConfirmedRequestsByEventIds(List<Long> eventIds);

    /**
     * Обновляет статус запроса.
     *
     * @param requestId идентификатор запроса
     * @param status    новый статус
     * @return обновленный запрос
     */
    ParticipationRequestDto updateRequestStatus(long requestId, Status status);
}