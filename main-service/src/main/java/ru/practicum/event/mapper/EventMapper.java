package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.utill.State;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Маппер для преобразования между сущностями и DTO событий.
 */
@UtilityClass
public class EventMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Преобразует DTO в сущность события.
     *
     * @param newEventDto DTO для создания
     * @return сущность события
     */
    public static Event mapFromNewEventDto(NewEventDto newEventDto) {
        if (newEventDto == null) {
            return null;
        }

        return Event.builder()
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .category(newEventDto.getCategoryObject())
                .eventDate(newEventDto.getEventDate())
                .initiator(newEventDto.getInitiatorObject())
                .location(newEventDto.getLocation())
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.isRequestModeration())
                .createdOn(LocalDateTime.now())
                .state(State.PENDING)
                .publishedOn(null)
                .views(0L)
                .confirmedRequests(0L)
                .build();
    }

    /**
     * Преобразует сущность в DTO полного события.
     *
     * @param event сущность события
     * @return DTO события
     */
    public static EventFullDto mapToEventFullDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(toCategoryDto(event.getCategory()))
                .initiator(toUserShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .eventDate(formatDateTime(event.getEventDate()))
                .createdOn(formatDateTime(event.getCreatedOn()))
                .publishedOn(formatDateTime(event.getPublishedOn()))
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .build();
    }

    /**
     * Преобразует сущность в DTO краткого события.
     *
     * @param event сущность события
     * @return DTO краткого события
     */
    public static EventShortDto mapToEventShortDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .initiator(toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .eventDate(formatDateTime(event.getEventDate()))
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .build();
    }

    /**
     * Преобразует сущность категории в DTO.
     *
     * @param category сущность категории
     * @return DTO категории
     */
    public static CategoryDto toCategoryDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    /**
     * Преобразует сущность пользователя в DTO.
     *
     * @param user сущность пользователя
     * @return DTO пользователя
     */
    public static UserShortDto toUserShortDto(User user) {
        if (user == null) {
            return null;
        }
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }

    /**
     * Преобразует сущность в DTO с внешними счетчиками.
     *
     * @param event             сущность события
     * @param confirmedRequests количество подтвержденных запросов
     * @param views             количество просмотров
     * @return DTO события
     */
    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) {
            return null;
        }

        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(toCategoryDto(event.getCategory()))
                .initiator(toUserShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .eventDate(formatDateTime(event.getEventDate()))
                .createdOn(formatDateTime(event.getCreatedOn()))
                .publishedOn(formatDateTime(event.getPublishedOn()))
                .confirmedRequests(confirmedRequests != null ? confirmedRequests : 0L)
                .views(views != null ? views : 0L)
                .build();
    }

    /**
     * Преобразует сущность в DTO краткого события с внешними счетчиками.
     *
     * @param event             сущность события
     * @param confirmedRequests количество подтвержденных запросов
     * @param views             количество просмотров
     * @return DTO краткого события
     */
    public static EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .initiator(toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .eventDate(formatDateTime(event.getEventDate()))
                .confirmedRequests(confirmedRequests != null ? confirmedRequests : 0L)
                .views(views != null ? views : 0L)
                .build();
    }

    /**
     * Обновляет сущность события из запроса администратора.
     *
     * @param event       сущность события
     * @param updateEvent запрос на обновление
     * @return обновленная сущность
     */
    public static Event updateEventFromAdminRequest(Event event, UpdateEventAdminRequest updateEvent) {
        if (updateEvent.hasAnnotation())
            event.setAnnotation(updateEvent.getAnnotation());

        if (updateEvent.hasCategory())
            event.setCategory(updateEvent.getCategoryObj());

        if (updateEvent.hasDescription())
            event.setDescription(updateEvent.getDescription());

        if (updateEvent.hasEventDate())
            event.setEventDate(updateEvent.getEventDate());

        if (updateEvent.hasLocation())
            event.setLocation(updateEvent.getLocation());

        if (updateEvent.hasPaid())
            event.setPaid(updateEvent.getPaid());

        if (updateEvent.hasParticipantLimit())
            event.setParticipantLimit(updateEvent.getParticipantLimit());

        if (updateEvent.hasRequestModeration())
            event.setRequestModeration(updateEvent.getRequestModeration());

        if (updateEvent.hasStateAction()) {
            switch (updateEvent.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(State.CANCELED);
            }
        }

        if (updateEvent.hasTitle())
            event.setTitle(updateEvent.getTitle());

        return event;
    }
}