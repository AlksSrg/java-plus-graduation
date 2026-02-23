package ru.practicum.event.model;

import lombok.*;

/**
 * Сущность местоположения.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private Float lat;
    private Float lon;
}