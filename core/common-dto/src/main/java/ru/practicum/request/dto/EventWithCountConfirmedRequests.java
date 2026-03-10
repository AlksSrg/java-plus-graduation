package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Проекция для получения количества подтверждённых запросов по событию.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventWithCountConfirmedRequests {
    private Long eventId;
    private Long count;
}