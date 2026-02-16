package ru.practicum.event.specification;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.event.model.Event;
import ru.practicum.event.util.State;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Спецификации для фильтрации событий.
 */
@UtilityClass
public class EventSpecification {

    /**
     * Фильтр по пользователям.
     *
     * @param users список идентификаторов пользователей
     * @return спецификация
     */
    public Specification<Event> byUsers(List<Long> users) {
        return (root, query, cb) -> root.get("initiatorId").in(users);
    }

    /**
     * Фильтр по статусам.
     *
     * @param states список статусов
     * @return спецификация
     */
    public Specification<Event> byStates(List<String> states) {
        return (root, query, cb) -> root.get("state").as(String.class).in(states);
    }

    /**
     * Фильтр по категориям.
     *
     * @param categories список идентификаторов категорий
     * @return спецификация
     */
    public Specification<Event> byCategories(List<Long> categories) {
        return (root, query, cb) -> root.get("categoryId").in(categories);
    }

    /**
     * Фильтр по начальной дате.
     *
     * @param rangeStart начальная дата
     * @return спецификация
     */
    public Specification<Event> byRangeStart(LocalDateTime rangeStart) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart);
    }

    /**
     * Фильтр по конечной дате.
     *
     * @param rangeEnd конечная дата
     * @return спецификация
     */
    public Specification<Event> byRangeEnd(LocalDateTime rangeEnd) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd);
    }

    /**
     * Фильтр по тексту.
     *
     * @param text текст для поиска
     * @return спецификация
     */
    public Specification<Event> byText(String text) {
        String searchPattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("annotation")), searchPattern),
                cb.like(cb.lower(root.get("description")), searchPattern)
        );
    }

    /**
     * Фильтр по признаку платности.
     *
     * @param paid признак платности
     * @return спецификация
     */
    public Specification<Event> byPaid(Boolean paid) {
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    /**
     * Фильтр только доступных событий.
     *
     * @return спецификация
     */
    public Specification<Event> onlyAvailable() {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("participantLimit"), 0),
                cb.greaterThan(root.get("participantLimit"), root.get("confirmedRequests"))
        );
    }

    /**
     * Фильтр только опубликованных событий.
     *
     * @return спецификация
     */
    public Specification<Event> publishedOnly() {
        return (root, query, cb) -> cb.equal(root.get("state"), State.PUBLISHED);
    }
}