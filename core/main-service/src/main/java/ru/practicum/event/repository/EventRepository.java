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
 * <p>
 * Предоставляет методы для выполнения операций с событиями, включая поиск по инициатору,
 * <p>
 * Проверку существования категории и поиск по списку идентификаторов.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /**
     * Находит события по идентификатору инициатора с пагинацией.
     *
     * @param initiatorId идентификатор инициатора события
     * @param pageable    параметры пагинации
     * @return страница событий
     */
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    /**
     * Находит событие по идентификатору и идентификатору инициатора.
     *
     * @param eventId     идентификатор события
     * @param initiatorId идентификатор инициатора
     * @return Optional с событием, если найдено
     */
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    /**
     * Находит события по списку идентификаторов.
     *
     * @param eventIds список идентификаторов событий
     * @return список событий
     */
    List<Event> findByIdIn(List<Long> eventIds);

    /**
     * Проверяет существование событий для указанной категории.
     *
     * @param categoryId идентификатор категории
     * @return true если существуют события для категории, иначе false
     */
    boolean existsByCategoryId(Long categoryId);
}