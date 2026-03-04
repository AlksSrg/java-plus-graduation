package ru.practicum.event.mapper;

import org.mapstruct.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.model.Event;
import ru.practicum.user.dto.UserShortDto;

/**
 * Маппер для преобразования между сущностью Event и DTO.
 */
@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoryId", source = "category")
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "paid", defaultValue = "false")
    @Mapping(target = "participantLimit", defaultValue = "0")
    @Mapping(target = "requestModeration", defaultValue = "true")
    Event toEntity(NewEventDto newEventDto);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "category", source = "categoryDto")
    @Mapping(target = "initiator", source = "userShortDto")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "rating", source = "rating")
    EventFullDto toFullDto(Event event, CategoryDto categoryDto, UserShortDto userShortDto,
                           Long confirmedRequests, Double rating);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "category", source = "categoryDto")
    @Mapping(target = "initiator", source = "userShortDto")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "views", ignore = true)
    EventShortDto toShortDto(Event event, CategoryDto categoryDto, UserShortDto userShortDto,
                             Long confirmedRequests, Double rating);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "categoryId", source = "category")
    @Mapping(target = "paid", source = "paid")
    @Mapping(target = "participantLimit", source = "participantLimit")
    @Mapping(target = "requestModeration", source = "requestModeration")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromUserRequest(UpdateEventUserRequest request, @MappingTarget Event event);
}