package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.request.utill.Status;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность заявки на участие в событии.
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

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}