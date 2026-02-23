package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.comment.dto.CommentDto;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с comment-service.
 * Позволяет другим микросервисам получать данные о комментариях.
 */
@FeignClient(name = "comment-service", path = "/events/{eventId}/comments")
public interface CommentClient {

    /**
     * Получает все комментарии для указанного события.
     * Используется другими сервисами (например, event-service) для отображения списка комментариев.
     *
     * @param eventId   идентификатор события
     * @param authorIds список идентификаторов авторов для фильтрации
     * @param from      количество элементов для пропуска
     * @param size      количество элементов для выборки
     * @return список DTO комментариев
     */
    @GetMapping
    List<CommentDto> getComments(@PathVariable("eventId") long eventId,
                                 @RequestParam(value = "authorIds", required = false) List<Long> authorIds,
                                 @RequestParam(value = "from", defaultValue = "0") Integer from,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size);
}