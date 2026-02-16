package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с событиями.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /**
     * Находит события по идентификатору инициатора.
     *
     * @param initiatorId идентификатор инициатора
     * @param pageable    параметры пагинации
     * @return страница событий
     */
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    /**
     * Находит событие по идентификатору и идентификатору инициатора.
     *
     * @param eventId     идентификатор события
     * @param initiatorId идентификатор инициатора
     * @return событие
     */
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    /**
     * Находит события по списку идентификаторов.
     *
     * @param eventIds список идентификаторов
     * @return список событий
     */
    List<Event> findByIdIn(List<Long> eventIds);

    /**
     * Проверяет существование событий для категории.
     *
     * @param categoryId идентификатор категории
     * @return true если есть события
     */
    boolean existsByCategoryId(Long categoryId);
}