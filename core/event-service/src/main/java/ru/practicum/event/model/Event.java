package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.util.State;

import java.time.LocalDateTime;

/**
 * Сущность события.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "location_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "location_lon")),
    })
    private Location location;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column(name = "participant_limit", nullable = false)
    @Builder.Default
    private Integer participantLimit = 0;

    @Column(name = "request_moderation", nullable = false)
    @Builder.Default
    private Boolean requestModeration = true;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private State state = State.PENDING;

    @Transient
    @Builder.Default
    private Double rating = 0.0;

    @Transient
    @Builder.Default
    private Long confirmedRequests = 0L;

    /**
     * Публикация события.
     */
    public void publish() {
        this.state = State.PUBLISHED;
        this.publishedOn = LocalDateTime.now();
    }

    /**
     * Отмена события.
     */
    public void cancel() {
        if (this.state != State.PUBLISHED) {
            this.state = State.CANCELED;
        }
    }

    /**
     * Отправка на модерацию.
     */
    public void sendToReview() {
        this.state = State.PENDING;
    }
}