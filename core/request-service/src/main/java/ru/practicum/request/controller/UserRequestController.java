package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.UserActionClient;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Контроллер для операций пользователей с запросами на участие.
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
     * Получает все запросы пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список запросов пользователя
     */
    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        log.info("GET /users/{}/requests", userId);
        return requestService.getRequestsByUserId(userId);
    }

    /**
     * Создает новый запрос на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return созданный запрос
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam Long eventId) {
        log.info("POST /users/{}/requests with eventId {}", userId, eventId);

        ParticipationRequestDto request = requestService.createRequest(userId, eventId);

        // Отправляем информацию о регистрации асинхронно
        executorService.submit(() -> {
            try {
                userActionClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER, Instant.now());
                log.debug("Sent REGISTER action for user {} event {}", userId, eventId);
            } catch (Exception e) {
                log.error("Error sending REGISTER action to collector for user {} event {}", userId, eventId, e);
            }
        });

        return request;
    }

    /**
     * Отменяет запрос на участие.
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор запроса
     * @return отмененный запрос
     */
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}