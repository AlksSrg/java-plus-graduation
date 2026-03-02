package ru.practicum.request.service;

import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с запросами на участие.
 * Определяет контракт для операций с запросами пользователей на участие в событиях.
 */
public interface RequestService {

    // ========== Методы для пользователей ==========

    /**
     * Получает все запросы указанного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список запросов
     */
    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    /**
     * Создаёт новый запрос на участие в событии.
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
     * @return отменённый запрос
     */
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    // ========== Методы для внутреннего взаимодействия (Feign-клиенты) ==========

    /**
     * Получает все запросы для указанного события.
     *
     * @param eventId идентификатор события
     * @return список запросов
     */
    List<ParticipationRequestDto> getRequestsByEventId(long eventId);

    /**
     * Получает запросы по списку их идентификаторов.
     *
     * @param ids список идентификаторов запросов
     * @return список запросов
     */
    List<ParticipationRequestDto> getRequestsByIds(List<Long> ids);

    /**
     * Подсчитывает количество подтверждённых запросов для события.
     *
     * @param eventId идентификатор события
     * @return количество подтверждённых запросов
     */
    Long countConfirmedRequestsByEventId(long eventId);

    /**
     * Подсчитывает количество подтверждённых запросов для списка событий.
     *
     * @param eventIds список идентификаторов событий
     * @return карта, где ключ — идентификатор события, значение — количество подтверждённых запросов
     */
    Map<Long, Long> countConfirmedRequestsByEventIds(List<Long> eventIds);

    /**
     * Обновляет статус одного запроса.
     *
     * @param requestId идентификатор запроса
     * @param status    новый статус
     * @return обновлённый запрос
     */
    ParticipationRequestDto updateRequestStatus(long requestId, Status status);

    /**
     * Массовое обновление статусов запросов (используется владельцем события).
     *
     * @param userId  идентификатор владельца события
     * @param eventId идентификатор события
     * @param request данные для обновления (список идентификаторов запросов и новый статус)
     * @return результат обновления: списки подтверждённых и отклонённых запросов
     */
    EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}