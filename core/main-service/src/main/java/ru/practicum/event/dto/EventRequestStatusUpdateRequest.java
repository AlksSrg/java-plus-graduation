package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.request.utill.Status;

import java.util.List;

/**
 * DTO для обновления статусов запросов на участие.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private Status status;
}