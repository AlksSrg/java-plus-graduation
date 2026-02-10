package ru.practicum.event.model;

import jakarta.persistence.Embeddable;
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
@Embeddable
public class Location {
    private Float lat;
    private Float lon;
}