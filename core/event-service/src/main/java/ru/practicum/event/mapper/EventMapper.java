package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.util.State;
import ru.practicum.user.dto.UserShortDto;

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
     * @param categoryId  идентификатор категории
     * @param initiatorId идентификатор инициатора
     * @return сущность события
     */
    public Event toEntity(NewEventDto newEventDto, Long categoryId, Long initiatorId) {
        if (newEventDto == null) {
            return null;
        }

        return Event.builder()
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .categoryId(categoryId)
                .eventDate(newEventDto.getEventDate())
                .initiatorId(initiatorId)
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
                .eventDate(formatDateTime(event.getEventDate()))
                .createdOn(formatDateTime(event.getCreatedOn()))
                .publishedOn(formatDateTime(event.getPublishedOn()))
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
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
                .eventDate(formatDateTime(event.getEventDate()))
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .build();
    }

    /**
     * Преобразует сущность в краткое DTO без дополнительных параметров.
     * Используется когда категория и пользователь уже загружены в сущности.
     *
     * @param event сущность события
     * @return краткое DTO события
     */
    public static EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .eventDate(formatDateTime(event.getEventDate()))
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .build();
    }

    /**
     * Преобразует сущность в полное DTO с внешними счетчиками.
     *
     * @param event             сущность события
     * @param categoryDto       DTO категории
     * @param userDto           DTO пользователя
     * @param confirmedRequests количество подтвержденных запросов
     * @param views             количество просмотров
     * @return полное DTO события
     */
    public EventFullDto toFullDto(Event event, CategoryDto categoryDto, UserShortDto userDto,
                                  Long confirmedRequests, Long views) {
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
                .eventDate(formatDateTime(event.getEventDate()))
                .createdOn(formatDateTime(event.getCreatedOn()))
                .publishedOn(formatDateTime(event.getPublishedOn()))
                .confirmedRequests(confirmedRequests != null ? confirmedRequests : 0L)
                .views(views != null ? views : 0L)
                .build();
    }

    /**
     * Преобразует сущность в краткое DTO с внешними счетчиками.
     *
     * @param event             сущность события
     * @param categoryDto       DTO категории
     * @param userDto           DTO пользователя
     * @param confirmedRequests количество подтвержденных запросов
     * @param views             количество просмотров
     * @return краткое DTO события
     */
    public EventShortDto toShortDto(Event event, CategoryDto categoryDto, UserShortDto userDto,
                                    Long confirmedRequests, Long views) {
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
                .eventDate(formatDateTime(event.getEventDate()))
                .confirmedRequests(confirmedRequests != null ? confirmedRequests : 0L)
                .views(views != null ? views : 0L)
                .build();
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }

    /**
     * Обновляет сущность из запроса администратора.
     *
     * @param event       сущность события
     * @param updateEvent запрос на обновление
     * @param categoryId  идентификатор категории
     * @return обновленная сущность
     */
    public Event updateFromAdmin(Event event, UpdateEventAdminRequest updateEvent, Long categoryId) {
        if (updateEvent.hasAnnotation()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }

        if (categoryId != null) {
            event.setCategoryId(categoryId);
        }

        if (updateEvent.hasDescription()) {
            event.setDescription(updateEvent.getDescription());
        }

        if (updateEvent.hasEventDate()) {
            event.setEventDate(updateEvent.getEventDate());
        }

        if (updateEvent.hasLocation()) {
            event.setLocation(updateEvent.getLocation());
        }

        if (updateEvent.hasPaid()) {
            event.setPaid(updateEvent.getPaid());
        }

        if (updateEvent.hasParticipantLimit()) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }

        if (updateEvent.hasRequestModeration()) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }

        if (updateEvent.hasTitle()) {
            event.setTitle(updateEvent.getTitle());
        }

        return event;
    }

    /**
     * Обновляет сущность из запроса пользователя.
     *
     * @param event       сущность события
     * @param updateEvent запрос на обновление
     * @param categoryId  идентификатор категории
     * @return обновленная сущность
     */
    public Event updateFromUser(Event event, UpdateEventUserRequest updateEvent, Long categoryId) {
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }

        if (categoryId != null) {
            event.setCategoryId(categoryId);
        }

        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }

        if (updateEvent.getEventDate() != null) {
            event.setEventDate(updateEvent.getEventDate());
        }

        if (updateEvent.getLocation() != null) {
            event.setLocation(updateEvent.getLocation());
        }

        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }

        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }

        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }

        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            event.setTitle(updateEvent.getTitle());
        }

        return event;
    }
}