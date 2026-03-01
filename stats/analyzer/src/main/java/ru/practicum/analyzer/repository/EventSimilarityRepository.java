package ru.practicum.analyzer.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Set;

/**
 * Репозиторий для работы со схожестью мероприятий.
 */
@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    /**
     * Находит записи по набору eventA.
     *
     * @param eventIds    набор ID
     * @param pageRequest параметры пагинации
     * @return список записей
     */
    List<EventSimilarity> findAllByEventAIn(Set<Long> eventIds, PageRequest pageRequest);

    /**
     * Находит записи по набору eventB.
     *
     * @param eventIds    набор ID
     * @param pageRequest параметры пагинации
     * @return список записей
     */
    List<EventSimilarity> findAllByEventBIn(Set<Long> eventIds, PageRequest pageRequest);

    /**
     * Находит записи по eventA.
     *
     * @param eventId     ID мероприятия
     * @param pageRequest параметры пагинации
     * @return список записей
     */
    List<EventSimilarity> findAllByEventA(Long eventId, PageRequest pageRequest);

    /**
     * Находит записи по eventB.
     *
     * @param eventId     ID мероприятия
     * @param pageRequest параметры пагинации
     * @return список записей
     */
    List<EventSimilarity> findAllByEventB(Long eventId, PageRequest pageRequest);
}