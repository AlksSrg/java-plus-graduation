package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

/**
 * Маппер для преобразования между сущностью Request и DTO.
 */
@Mapper(componentModel = "spring")
public interface RequestMapper {

    /**
     * Преобразует сущность запроса в DTO.
     *
     * @param request сущность запроса
     * @return DTO запроса
     */
    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toDto(Request request);

    /**
     * Преобразует список сущностей в список DTO.
     *
     * @param requests список сущностей
     * @return список DTO
     */
    List<ParticipationRequestDto> toDtoList(List<Request> requests);

    /**
     * Создаёт результат обновления статусов запросов.
     */
    default EventRequestStatusUpdateResult toUpdateResult(
            List<ParticipationRequestDto> confirmedRequests,
            List<ParticipationRequestDto> rejectedRequests) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }
}