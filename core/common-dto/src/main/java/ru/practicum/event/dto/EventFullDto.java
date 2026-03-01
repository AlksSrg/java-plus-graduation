package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.model.Location;
import ru.practicum.event.util.State;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

/**
 * DTO для полного представления события.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long id;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;

    @JsonProperty(defaultValue = "0")
    private Integer participantLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    private State state;
    private String title;
    private Long views;
    private Double rating;
}