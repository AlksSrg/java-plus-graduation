package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.config.FeignConfiguration;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с comment-service.
 * Позволяет получать комментарии к событиям.
 */
@FeignClient(name = "comment-service", path = "/events/{eventId}/comments", configuration = FeignConfiguration.class)
public interface CommentClient {

    /**
     * Получает все комментарии для указанного события.
     *
     * @param eventId   идентификатор события
     * @param authorIds список идентификаторов авторов для фильтрации (опционально)
     * @param from      смещение для пагинации
     * @param size      количество элементов на странице
     * @return список DTO комментариев
     */
    @GetMapping
    List<CommentDto> getComments(@PathVariable("eventId") long eventId,
                                 @RequestParam(value = "authorIds", required = false) List<Long> authorIds,
                                 @RequestParam(value = "from", defaultValue = "0") Integer from,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size);
}