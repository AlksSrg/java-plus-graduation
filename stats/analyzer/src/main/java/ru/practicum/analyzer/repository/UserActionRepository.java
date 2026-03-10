package ru.practicum.analyzer.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.model.UserAction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Репозиторий для работы с действиями пользователей.
 */
@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    /**
     * Находит действия пользователя.
     *
     * @param userId      ID пользователя
     * @param pageable параметры пагинации
     * @return список действий
     */
    List<UserAction> findAllByUserId(Long userId, Pageable pageable);

    /**
     * Находит действия по набору мероприятий для пользователя.
     *
     * @param eventIds набор ID мероприятий
     * @param userId   ID пользователя
     * @return список действий
     */
    List<UserAction> findAllByEventIdInAndUserId(Set<Long> eventIds, Long userId);

    /**
     * Находит все действия для мероприятий.
     *
     * @param eventIds набор ID мероприятий
     * @return список действий
     */
    List<UserAction> findAllByEventIdIn(Set<Long> eventIds);

    /**
     * Находит действие пользователя для мероприятия.
     *
     * @param userId  ID пользователя
     * @param eventId ID мероприятия
     * @return Optional с действием
     */
    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);

    /**
     * Проверяет наличие действия.
     *
     * @param eventId ID мероприятия
     * @param userId  ID пользователя
     * @return true если действие существует
     */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}