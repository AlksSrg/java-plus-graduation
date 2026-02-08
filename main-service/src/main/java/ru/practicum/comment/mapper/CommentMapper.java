package ru.practicum.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.mapper.UserMapper;

/**
 * Утилитарный класс для преобразования между сущностями Comment и DTO.
 */
@UtilityClass
public class CommentMapper {

    /**
     * Преобразует DTO для создания комментария в сущность Comment.
     *
     * @param commentDto DTO с данными для создания комментария
     * @return сущность Comment, подготовленная для сохранения в базу данных
     * @throws IllegalArgumentException если commentDto равен null
     */
    public static Comment mapFromNewDto(NewCommentDto commentDto) {
        if (commentDto == null) {
            throw new IllegalArgumentException("NewCommentDto не может быть null");
        }

        return Comment.builder()
                .author(commentDto.getAuthorObj())
                .event(commentDto.getEventObj())
                .created(commentDto.getCreated())
                .text(commentDto.getText())
                .build();
    }

    /**
     * Преобразует сущность Comment в DTO для ответа.
     * Включает преобразование связанных сущностей User и Event в соответствующие DTO.
     *
     * @param comment сущность комментария из базы данных
     * @return DTO комментария для возврата в API
     * @throws IllegalArgumentException если comment равен null
     */
    public static CommentDto mapFromComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment не может быть null");
        }

        return CommentDto.builder()
                .id(comment.getId())
                .author(UserMapper.mapToDto(comment.getAuthor()))
                .event(EventMapper.mapToEventShortDto(comment.getEvent()))
                .created(comment.getCreated())
                .text(comment.getText())
                .build();
    }
}