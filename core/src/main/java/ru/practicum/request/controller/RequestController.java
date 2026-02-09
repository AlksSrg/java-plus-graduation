package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

/**
 * Контроллер для операций с заявками на участие в событиях.
 */
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    /**
     * Получает все заявки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список заявок
     */
    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        return requestService.getRequestsByUserId(userId);
    }

    /**
     * Создает новую заявку на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return созданная заявка
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam @Positive Long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    /**
     * Отменяет заявку на участие.
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор заявки
     * @return отмененная заявка
     */
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}