package ru.practicum.event.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Параметры для получения событий публичным API.
 */
@Builder
@Getter
@Setter
public class EventGetPublicParam {

    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private String sort;
    private int from;
    private int size;

    /**
     * Возвращает список статусов для публичного доступа (только PUBLISHED).
     *
     * @return список статусов
     */
    public List<String> getStates() {
        return List.of(State.PUBLISHED.toString());
    }
}