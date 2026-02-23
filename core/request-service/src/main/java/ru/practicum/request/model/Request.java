package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.request.util.Status;

import java.time.LocalDateTime;

/**
 * Сущность запроса на участие в событии.
 */
@Builder
@Table(name = "requests")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return id != null && id.equals(request.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}