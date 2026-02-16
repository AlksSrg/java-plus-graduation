package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.Request;
import ru.practicum.request.util.Status;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с запросами на участие.
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    /**
     * Находит запросы по идентификатору события.
     *
     * @param eventId идентификатор события
     * @return список запросов
     */
    List<Request> findByEventId(Long eventId);

    /**
     * Находит запросы по идентификатору пользователя.
     *
     * @param requesterId идентификатор пользователя
     * @return список запросов
     */
    List<Request> findByRequesterId(Long requesterId);

    /**
     * Находит запрос по идентификатору пользователя и идентификатору события.
     *
     * @param requesterId идентификатор пользователя
     * @param eventId     идентификатор события
     * @return запрос
     */
    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    /**
     * Подсчитывает количество запросов для события с указанным статусом.
     *
     * @param eventId идентификатор события
     * @param status  статус
     * @return количество запросов
     */
    Long countByEventIdAndStatus(Long eventId, Status status);

    /**
     * Подсчитывает количество запросов для списка событий с указанным статусом.
     *
     * @param eventIds список идентификаторов событий
     * @param status   статус
     * @return список объектов [eventId, count]
     */
    @Query("SELECT r.eventId, COUNT(r) FROM Request r WHERE r.eventId IN :eventIds AND r.status = :status GROUP BY r.eventId")
    List<Object[]> countByEventIdInAndStatus(@Param("eventIds") List<Long> eventIds, @Param("status") Status status);
}