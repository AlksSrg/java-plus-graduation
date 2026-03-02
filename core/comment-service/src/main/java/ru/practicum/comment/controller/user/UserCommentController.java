package ru.practicum.comment.controller.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

/**
 * Контроллер для операций с комментариями от имени пользователя.
 * Все методы требуют аутентификации (подразумевается, что userId взят из контекста).
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class UserCommentController {

    private final CommentService commentService;

    /**
     * Получить конкретный комментарий пользователя.
     *
     * @param userId    идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return комментарий
     */
    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable @Positive Long userId,
                                 @PathVariable @Positive Long commentId) {
        log.debug("User: запрос комментария userId={}, commentId={}", userId, commentId);
        return commentService.getUserComment(userId, commentId);
    }

    /**
     * Получить все комментарии пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список комментариев
     */
    @GetMapping
    public List<CommentDto> getUserComments(@PathVariable @Positive Long userId) {
        log.debug("User: запрос всех комментариев пользователя userId={}", userId);
        return commentService.getUserComments(userId);
    }

    /**
     * Создать новый комментарий к событию.
     *
     * @param userId  идентификатор автора
     * @param eventId идентификатор события
     * @param dto     данные для создания
     * @return созданный комментарий
     */
    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long eventId,
                                    @RequestBody @Valid NewCommentDto dto) {
        log.debug("User: создание комментария userId={}, eventId={}", userId, eventId);
        return commentService.createComment(userId, eventId, dto);
    }

    /**
     * Обновить существующий комментарий.
     *
     * @param userId    идентификатор автора
     * @param commentId идентификатор комментария
     * @param dto       данные для обновления
     * @return обновлённый комментарий
     */
    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long commentId,
                                    @RequestBody @Valid UpdateCommentDto dto) {
        log.debug("User: обновление комментария userId={}, commentId={}", userId, commentId);
        return commentService.updateComment(userId, commentId, dto);
    }

    /**
     * Удалить свой комментарий.
     *
     * @param userId    идентификатор автора
     * @param commentId идентификатор комментария
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long userId,
                              @PathVariable @Positive Long commentId) {
        log.debug("User: удаление комментария userId={}, commentId={}", userId, commentId);
        commentService.deleteCommentByUser(userId, commentId);
    }
}