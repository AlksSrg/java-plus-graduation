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
@RequestMapping("/users/{userId}/comments")
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
    public CommentDto get(@PathVariable @Positive Long userId,
                          @PathVariable @Positive Long commentId) {
        return commentService.get(userId, commentId);
    }

    /**
     * Получает все комментарии пользователя.
     *
     * @param userId идентификатор пользователя, должен быть положительным числом
     * @return список DTO комментариев пользователя, может быть пустым
     */
    @GetMapping
    public List<CommentDto> getAll(@PathVariable @Positive Long userId) {
        return commentService.getAll(userId);
    }

    /**
     * Создает новый комментарий к событию.
     *
     * @param userId     идентификатор пользователя, должен быть положительным числом
     * @param eventId    идентификатор события, должен быть положительным числом
     * @param commentDto DTO с данными для создания комментария
     * @return созданный DTO комментария
     */
    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable @Positive Long userId,
                             @PathVariable @Positive Long eventId,
                             @RequestBody @Valid NewCommentDto commentDto) {
        commentDto.setAuthorId(userId);
        commentDto.setEventId(eventId);
        return commentService.create(commentDto);
    }

    /**
     * Обновляет существующий комментарий.
     *
     * @param userId     идентификатор пользователя, должен быть положительным числом
     * @param commentId  идентификатор комментария, должен быть положительным числом
     * @param commentDto DTO с данными для обновления комментария
     * @return обновленный DTO комментария
     */
    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable @Positive Long userId,
                             @PathVariable @Positive Long commentId,
                             @RequestBody @Valid UpdateCommentDto commentDto) {
        commentDto.setAuthorId(userId);
        commentDto.setCommentId(commentId);
        return commentService.update(commentDto);
    }

    /**
     * Удаляет комментарий.
     *
     * @param userId    идентификатор пользователя, должен быть положительным числом
     * @param commentId идентификатор комментария, должен быть положительным числом
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long userId,
                       @PathVariable @Positive Long commentId) {
        commentService.delete(userId, commentId);
    }
}