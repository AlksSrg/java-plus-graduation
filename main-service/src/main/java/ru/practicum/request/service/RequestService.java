package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

/**
 * Сервис для работы с заявками на участие в событиях.
 */
public interface RequestService {

    /**
     * Получает все заявки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список заявок
     */
    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    /**
     * Создает новую заявку на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return созданная заявка
     */
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    /**
     * Отменяет заявку на участие.
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор заявки
     * @return отмененная заявка
     */
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}