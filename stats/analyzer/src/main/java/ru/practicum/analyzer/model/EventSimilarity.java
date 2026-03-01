package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Сущность схожести мероприятий.
 */
@Entity
@Table(name = "events_similarity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventSimilarity {

    /**
     * Идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * Первое мероприятие.
     */
    @Column(name = "event_a", nullable = false)
    Long eventA;

    /**
     * Второе мероприятие.
     */
    @Column(name = "event_b", nullable = false)
    Long eventB;

    /**
     * Оценка схожести.
     */
    @Column(name = "score", nullable = false)
    Double score;

    /**
     * Время расчета.
     */
    @Column(name = "timestamp", nullable = false)
    Instant timestamp;
}