package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.request.dto.EventWithCountConfirmedRequests;
import ru.practicum.request.model.Request;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с запросами на участие.
 * Предоставляет методы для доступа к данным запросов в базе данных.
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    /**
     * Находит все запросы для указанного события.
     *
     * @param eventId идентификатор события
     * @return список запросов
     */
    List<Request> findByEventId(Long eventId);

    /**
     * Находит все запросы указанного пользователя.
     *
     * @param requesterId идентификатор пользователя
     * @return список запросов
     */
    List<Request> findByRequesterId(Long requesterId);

    /**
     * Находит запрос по идентификатору пользователя и события.
     *
     * @param requesterId идентификатор пользователя
     * @param eventId     идентификатор события
     * @return Optional с запросом, если найден
     */
    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    /**
     * Подсчитывает количество запросов для события с определённым статусом.
     *
     * @param eventId идентификатор события
     * @param status  статус запроса
     * @return количество запросов
     */
    Long countByEventIdAndStatus(Long eventId, Status status);

    /**
     * Подсчитывает количество подтверждённых запросов для списка событий.
     *
     * @param eventIds список идентификаторов событий
     * @param status   статус запроса (обычно CONFIRMED)
     * @return список объектов EventWithCountConfirmedRequests, содержащих eventId и количество
     */
    @Query("SELECT new ru.practicum.request.dto.EventWithCountConfirmedRequests(r.eventId, COUNT(r)) " +
            "FROM Request r WHERE r.eventId IN :eventIds AND r.status = :status GROUP BY r.eventId")
    List<EventWithCountConfirmedRequests> countByEventIdInAndStatus(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") Status status);

    /**
     * Находит все запросы по списку идентификаторов.
     *
     * @param ids список идентификаторов запросов
     * @return список запросов
     */
    List<Request> findAllByIdIn(List<Long> ids);

    /**
     * Находит все запросы для события с определённым статусом.
     *
     * @param eventId идентификатор события
     * @param status  статус запроса
     * @return список запросов
     */
    List<Request> findAllByEventIdAndStatus(Long eventId, Status status);
}