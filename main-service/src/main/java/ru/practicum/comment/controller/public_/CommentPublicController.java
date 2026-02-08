package ru.practicum.comment.controller.public_;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.comment.utill.CommentGetParam;
import ru.practicum.comment.utill.SortOrder;
import ru.practicum.exception.BadRequestException;

import java.util.List;

/**
 * Публичный контроллер для работы с комментариями.
 * Предоставляет API для получения комментариев без аутентификации.
 */
@Validated
@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
public class CommentPublicController {
    private final CommentService commentService;

    /**
     * Получает все комментарии для указанного события.
     * Доступно для всех пользователей без аутентификации.
     *
     * @param eventId   идентификатор события, должен быть положительным числом
     * @param authorIds перечень интересующих авторов
     * @param sortBy    порядок сортировки
     * @param from      сколько значений пропустить
     * @param size      кол-во элементов
     * @return список DTO комментариев события, может быть пустым
     */
    @GetMapping
    public List<CommentDto> getComments(@PathVariable @Positive long eventId,
                                        @RequestParam(required = false) List<Long> authorIds,
                                        @RequestParam(required = false) String sortBy,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                        @RequestParam(defaultValue = "10") @Positive Integer size) {
        CommentGetParam param;

        try {
            param = CommentGetParam.builder()
                    .eventId(eventId)
                    .authorIds(authorIds)
                    .sortBy(sortBy == null ? null : SortOrder.valueOf(sortBy.toUpperCase()))
                    .from(from)
                    .size(size)
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Недопустимое значение параметра сортировки");
        }

        return commentService.getComments(param);
    }
}