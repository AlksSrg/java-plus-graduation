package ru.practicum.event.specification;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Спецификации для фильтрации событий.
 */
@UtilityClass
public class EventSpecification {

    public static Specification<Event> byUser(List<Long> users) {
        return (root, cq, cb) -> root.get("initiator").get("id").in(users);
    }

    public static Specification<Event> byStates(List<String> states) {
        return (root, cq, cb) -> root.get("state").as(String.class).in(states);
    }

    public static Specification<Event> byCategories(List<Long> categories) {
        return (root, cq, cb) -> root.get("category").get("id").in(categories);
    }

    public static Specification<Event> byRangeStart(LocalDateTime rangeStart) {
        return (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart);
    }

    public static Specification<Event> byRangeEnd(LocalDateTime rangeEnd) {
        return (root, cq, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd);
    }

    public static Specification<Event> byText(String text) {
        String searchPattern = "%" + text.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(cb.like(cb.lower(root.get("annotation")), searchPattern),
                cb.like(cb.lower(root.get("description")), searchPattern));
    }

    public static Specification<Event> byPaid(boolean paid) {
        return (root, cq, cb) -> cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> byOnlyAvailable() {
        return (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("participantLimit"), 0);
    }
}