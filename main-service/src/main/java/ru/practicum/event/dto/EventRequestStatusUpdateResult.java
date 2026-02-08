package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

/**
 * DTO для результата обновления статусов запросов.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;
}