package ru.practicum.request.dto;

import lombok.*;
import ru.practicum.request.utill.Status;

/**
 * DTO для представления заявки на участие в событии.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    private String created;
    private Long event;
    private Long id;
    private Long requester;
    private Status status;
}