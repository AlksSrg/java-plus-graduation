package ru.practicum.comment.controller.public_;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.comment.util.CommentGetParam;
import ru.practicum.comment.util.SortOrder;
import ru.practicum.exception.BadRequestException;

import java.util.List;

/**
 * Публичный контроллер для получения комментариев к событиям.
 * Доступен без аутентификации.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
public class PublicCommentController {

    private final CommentService commentService;

    /**
     * Получить комментарии для события с возможностью фильтрации и пагинации.
     *
     * @param eventId   идентификатор события
     * @param authorIds список идентификаторов авторов (опционально)
     * @param sortBy    тип сортировки (CREATED, AUTHOR)
     * @param from      смещение
     * @param size      количество элементов
     * @return список комментариев
     */
    @GetMapping
    public List<CommentDto> getCommentsForEvent(@PathVariable @Positive Long eventId,
                                                @RequestParam(required = false) List<Long> authorIds,
                                                @RequestParam(required = false) String sortBy,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.debug("Public: запрос комментариев для события eventId={}", eventId);

        SortOrder sortOrder = null;
        if (sortBy != null) {
            try {
                sortOrder = SortOrder.valueOf(sortBy.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Недопустимое значение параметра сортировки: " + sortBy);
            }
        }

        CommentGetParam param = CommentGetParam.builder()
                .eventId(eventId)
                .authorIds(authorIds)
                .sortBy(sortOrder)
                .from(from)
                .size(size)
                .build();

        return commentService.getCommentsForEvent(param);
    }
}