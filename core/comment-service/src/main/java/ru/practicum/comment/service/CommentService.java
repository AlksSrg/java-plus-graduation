package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.util.CommentGetParam;

import java.util.List;

/**
 * Сервис для управления комментариями.
 * Определяет операции для пользователей, публичного доступа и администраторов.
 */
public interface CommentService {

    /**
     * Получение конкретного комментария пользователя.
     *
     * @param userId    идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return DTO комментария с полными данными
     */
    CommentDto getUserComment(Long userId, Long commentId);

    /**
     * Получение всех комментариев пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список комментариев
     */
    List<CommentDto> getUserComments(Long userId);

    /**
     * Создание нового комментария к событию.
     *
     * @param userId  идентификатор автора
     * @param eventId идентификатор события
     * @param dto     данные для создания
     * @return созданный комментарий
     */
    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    /**
     * Обновление существующего комментария.
     *
     * @param userId    идентификатор автора
     * @param commentId идентификатор комментария
     * @param dto       данные для обновления
     * @return обновлённый комментарий
     */
    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    /**
     * Удаление комментария пользователем.
     *
     * @param userId    идентификатор автора
     * @param commentId идентификатор комментария
     */
    void deleteCommentByUser(Long userId, Long commentId);

    /**
     * Удаление комментария администратором.
     *
     * @param commentId идентификатор комментария
     */
    void deleteCommentByAdmin(Long commentId);

    /**
     * Получение комментариев для события с фильтрацией и пагинацией.
     *
     * @param param параметры запроса (eventId, authorIds, сортировка, пагинация)
     * @return список комментариев
     */
    List<CommentDto> getCommentsForEvent(CommentGetParam param);
}