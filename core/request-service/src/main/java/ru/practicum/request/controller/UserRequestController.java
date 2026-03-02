package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.UserActionClient;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Контроллер для операций пользователей с запросами на участие.
 * Предоставляет API для создания, отмены и просмотра запросов от имени текущего пользователя.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class UserRequestController {

    private final RequestService requestService;
    private final UserActionClient userActionClient;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Получает все запросы текущего пользователя.
     *
     * @param userId идентификатор пользователя (из пути)
     * @return список запросов пользователя
     */
    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        log.info("GET /users/{}/requests", userId);
        return requestService.getRequestsByUserId(userId);
    }

    /**
     * Создаёт новый запрос на участие в событии.
     *
     * @param userId  идентификатор пользователя (из пути)
     * @param eventId идентификатор события (параметр запроса)
     * @return созданный запрос
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam Long eventId) {
        log.info("POST /users/{}/requests with eventId {}", userId, eventId);

        ParticipationRequestDto request = requestService.createRequest(userId, eventId);

        // Асинхронная отправка действия регистрации в stats-collector
        executorService.submit(() -> {
            try {
                userActionClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER, Instant.now());
                log.debug("Отправлено ACTION_REGISTER для пользователя {} события {}", userId, eventId);
            } catch (Exception e) {
                log.error("Ошибка отправки ACTION_REGISTER", e);
            }
        });

        return request;
    }

    /**
     * Отменяет запрос на участие.
     *
     * @param userId    идентификатор пользователя (из пути)
     * @param requestId идентификатор запроса (из пути)
     * @return отменённый запрос
     */
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}