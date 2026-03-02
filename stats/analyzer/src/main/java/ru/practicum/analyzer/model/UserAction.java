package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.analyzer.enums.ActionType;

import java.time.Instant;

/**
 * Сущность действия пользователя.
 */
@Entity
@Table(name = "users_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAction {

    /**
     * Идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * Идентификатор пользователя.
     */
    @Column(name = "user_id", nullable = false)
    Long userId;

    /**
     * Идентификатор мероприятия.
     */
    @Column(name = "event_id", nullable = false)
    Long eventId;

    /**
     * Тип действия.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    ActionType actionType;

    /**
     * Время действия.
     */
    @Column(name = "timestamp", nullable = false)
    Instant timestamp;
}