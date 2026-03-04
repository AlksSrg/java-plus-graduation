package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.ArrayList;
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

    @Builder.Default
    private List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();

    @Builder.Default
    private List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
}