package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.util.State;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

/**
 * Маппер для преобразования между сущностями и DTO событий.
 */
@Component
public class EventMapper {

    /**
     * Преобразует DTO в сущность события.
     *
     * @param newEventDto DTO для создания
     * @return сущность события
     */
    public Event toEntity(NewEventDto newEventDto) {
        if (newEventDto == null) {
            return null;
        }

        return Event.builder()
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .createdOn(LocalDateTime.now())
                .state(State.PENDING)
                .confirmedRequests(0L)
                .rating(0.0)
                .build();
    }

    /**
     * Преобразует сущность в полное DTO.
     *
     * @param event       сущность события
     * @param categoryDto DTO категории
     * @param userDto     DTO пользователя
     * @return полное DTO события
     */
    public EventFullDto toFullDto(Event event, CategoryDto categoryDto, UserShortDto userDto) {
        if (event == null) {
            return null;
        }

        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(categoryDto)
                .initiator(userDto)
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .confirmedRequests(event.getConfirmedRequests())
                .rating(event.getRating())
                .build();
    }

    /**
     * Преобразует сущность в краткое DTO.
     *
     * @param event       сущность события
     * @param categoryDto DTO категории
     * @param userDto     DTO пользователя
     * @return краткое DTO события
     */
    public EventShortDto toShortDto(Event event, CategoryDto categoryDto, UserShortDto userDto) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(categoryDto)
                .initiator(userDto)
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .rating(event.getRating())
                .build();
    }

    /**
     * Преобразует сущность в полное DTO без дополнительных параметров.
     * Используется когда категория и пользователь уже загружены в сущности.
     *
     * @param event сущность события
     * @return полное DTO события
     */
    public EventFullDto toFullDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .confirmedRequests(event.getConfirmedRequests())
                .rating(event.getRating())
                .build();
    }

    /**
     * Преобразует сущность в краткое DTO без дополнительных параметров.
     * Используется когда категория и пользователь уже загружены в сущности.
     *
     * @param event сущность события
     * @return краткое DTO события
     */
    public EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .rating(event.getRating())
                .build();
    }
}