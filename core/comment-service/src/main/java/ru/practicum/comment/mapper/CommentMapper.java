package ru.practicum.comment.mapper;

import org.mapstruct.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;

/**
 * Маппер для преобразования между сущностью Comment и DTO.
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    /**
     * Преобразует NewCommentDto в сущность Comment.
     * Поля authorId, eventId, id, created игнорируются (заполняются в сервисе).
     *
     * @param newCommentDto DTO с данными для создания
     * @return сущность Comment без идентификаторов и даты
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "sort", ignore = true)
    Comment toEntity(NewCommentDto newCommentDto);

    /**
     * Преобразует сущность Comment в CommentDto.
     * Поля author и event остаются null, заполняются в сервисе.
     *
     * @param comment сущность комментария
     * @return DTO с базовыми полями
     */
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    CommentDto toDto(Comment comment);

    /**
     * Обновляет существующую сущность из UpdateCommentDto.
     * Игнорирует все поля, кроме text.
     *
     * @param updateCommentDto DTO с обновлённым текстом
     * @param comment          обновляемая сущность
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "sort", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateCommentDto updateCommentDto, @MappingTarget Comment comment);
}