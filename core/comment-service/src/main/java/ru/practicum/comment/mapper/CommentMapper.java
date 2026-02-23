package ru.practicum.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.feignclients.client.EventClient;
import ru.practicum.feignclients.client.UserClient;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования объектов комментария между различными слоями приложения.
 * Использует Feign clients для получения данных о пользователях и событиях.
 */
@Component
public class CommentMapper {

    private final UserClient userClient;
    private final EventClient eventClient;

    public CommentMapper(UserClient userClient, EventClient eventClient) {
        this.userClient = userClient;
        this.eventClient = eventClient;
    }

    /**
     * Преобразует NewCommentDto в сущность Comment.
     *
     * @param newCommentDto DTO с данными для создания комментария
     * @param authorId      ID автора комментария
     * @return сущность Comment
     */
    public Comment toEntity(NewCommentDto newCommentDto, Long authorId) {
        if (newCommentDto == null) {
            return null;
        }

        return Comment.builder()
                .authorId(authorId)
                .eventId(newCommentDto.getEventId())
                .text(newCommentDto.getText())
                .created(LocalDateTime.now())
                .build();
    }

    /**
     * Преобразует сущность Comment в CommentDto с полными данными.
     * Загружает данные о пользователе и событии через Feign clients.
     *
     * @param comment сущность комментария
     * @return CommentDto с полными данными
     */
    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        // Получаем данные об авторе через UserClient
        UserDto author = userClient.getUserById(comment.getAuthorId());

        // Получаем полные данные о событии через EventClient и преобразуем в краткие
        EventFullDto eventFull = eventClient.getEventById(comment.getEventId());
        EventShortDto event = convertToShortDto(eventFull);

        return CommentDto.builder()
                .id(comment.getId())
                .author(author)
                .event(event)
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }

    /**
     * Преобразует полное DTO события в краткое.
     *
     * @param eventFull полное DTO события
     * @return краткое DTO события
     */
    private EventShortDto convertToShortDto(EventFullDto eventFull) {
        if (eventFull == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(eventFull.getId())
                .annotation(eventFull.getAnnotation())
                .category(eventFull.getCategory())
                .confirmedRequests(eventFull.getConfirmedRequests())
                .eventDate(eventFull.getEventDate())
                .initiator(eventFull.getInitiator())
                .paid(eventFull.getPaid())
                .title(eventFull.getTitle())
                .views(eventFull.getViews())
                .build();
    }

    /**
     * Преобразует список сущностей Comment в список CommentDto.
     *
     * @param comments список сущностей комментариев
     * @return список CommentDto
     */
    public List<CommentDto> toDtoList(List<Comment> comments) {
        if (comments == null) {
            return List.of();
        }

        return comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Обновляет существующую сущность Comment данными из UpdateCommentDto.
     *
     * @param comment   существующая сущность комментария
     * @param updateDto DTO с данными для обновления
     * @return обновленная сущность Comment
     */
    public Comment updateEntity(Comment comment, UpdateCommentDto updateDto) {
        if (updateDto == null || comment == null) {
            return comment;
        }

        if (updateDto.getText() != null) {
            comment.setText(updateDto.getText());
        }

        return comment;
    }

    /**
     * Преобразует сущность Comment в CommentDto для публичного доступа.
     * (без проверки прав доступа)
     *
     * @param comment сущность комментария
     * @return CommentDto
     */
    public CommentDto toPublicDto(Comment comment) {
        return toDto(comment);
    }

    /**
     * Создает базовый CommentDto без загрузки связанных данных.
     * Используется когда не нужны полные данные о пользователе и событии.
     *
     * @param comment сущность комментария
     * @return CommentDto только с ID и основными полями
     */
    public CommentDto toBasicDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }
}