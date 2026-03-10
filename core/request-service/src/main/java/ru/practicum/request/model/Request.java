package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.request.util.Status;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность запроса на участие в событии.
 */
@Entity
@Table(name = "requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private LocalDateTime created;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    /**
     * Подтверждает запрос (устанавливает статус CONFIRMED).
     */
    public void confirm() {
        this.status = Status.CONFIRMED;
    }

    /**
     * Отклоняет запрос (устанавливает статус REJECTED).
     */
    public void reject() {
        this.status = Status.REJECTED;
    }

    /**
     * Отменяет запрос (устанавливает статус CANCELED).
     */
    public void cancel() {
        this.status = Status.CANCELED;
    }
}