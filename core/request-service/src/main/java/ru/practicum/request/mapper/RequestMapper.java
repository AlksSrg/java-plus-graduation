package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

/**
 * Маппер для преобразования между сущностью Request и DTO.
 */
@UtilityClass
public class RequestMapper {

    /**
     * Преобразует сущность в DTO.
     *
     * @param request сущность запроса
     * @return DTO запроса
     */
    public ParticipationRequestDto toDto(Request request) {
        if (request == null) {
            return null;
        }
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }
}