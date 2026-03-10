package ru.practicum.comment.controller.admin;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;

/**
 * Контроллер для административных операций с комментариями.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    /**
     * Удалить любой комментарий (административное действие).
     *
     * @param commentId идентификатор комментария
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long commentId) {
        log.debug("Admin: удаление комментария commentId={}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }
}