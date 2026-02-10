package ru.practicum.comment.controller.private_;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

/**
 * Приватный контроллер для работы с комментариями.
 * Предоставляет API для управления комментариями авторизованных пользователей.
 */
@Validated
@RestController
@RequestMapping("users/{userId}/comments")
@RequiredArgsConstructor
public class CommentPrivateController {
    private final CommentService commentService;

    /**
     * Получает конкретный комментарий пользователя.
     *
     * @param userId    идентификатор пользователя, должен быть положительным числом
     * @param commentId идентификатор комментария, должен быть положительным числом
     * @return DTO запрошенного комментария
     */
    @GetMapping("/{commentId}")
    public CommentDto get(@PathVariable @Positive long userId,
                          @PathVariable @Positive long commentId) {
        return commentService.get(userId, commentId);
    }

    /**
     * Получает все комментарии пользователя.
     *
     * @param userId идентификатор пользователя, должен быть положительным числом
     * @return список DTO комментариев пользователя, может быть пустым
     */
    @GetMapping()
    public List<CommentDto> getAll(@PathVariable @Positive long userId) {
        return commentService.getAll(userId);
    }

    /**
     * Создает новый комментарий к событию.
     *
     * @param userId  идентификатор пользователя, должен быть положительным числом
     * @param eventId идентификатор события, должен быть положительным числом
     * @param comment DTO с данными для создания комментария
     * @return созданный DTO комментария
     */
    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable @Positive long userId,
                             @PathVariable @Positive long eventId,
                             @RequestBody @Valid NewCommentDto comment) {
        comment.setAuthor(userId);
        comment.setEvent(eventId);
        return commentService.create(comment);
    }

    /**
     * Обновляет существующий комментарий.
     *
     * @param userId    идентификатор пользователя, должен быть положительным числом
     * @param commentId идентификатор комментария, должен быть положительным числом
     * @param comment   DTO с данными для обновления комментария
     * @return обновленный DTO комментария
     */
    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable @Positive long userId,
                             @PathVariable @Positive long commentId,
                             @RequestBody @Valid UpdateCommentDto comment) {
        comment.setAuthor(userId);
        comment.setCommentId(commentId);
        return commentService.update(comment);
    }

    /**
     * Удаляет комментарий.
     *
     * @param userId    идентификатор пользователя, должен быть положительным числом
     * @param commentId идентификатор комментария, должен быть положительным числом
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long userId,
                       @PathVariable @Positive long commentId) {
        commentService.delete(userId, commentId);
    }
}