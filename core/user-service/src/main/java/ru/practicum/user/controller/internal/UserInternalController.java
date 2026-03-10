package ru.practicum.user.controller.internal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.service.UserService;

/**
 * Внутренний контроллер для межсервисного взаимодействия.
 * Доступен только для других микросервисов.
 */
@Slf4j
@RestController
@RequestMapping("/users/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    /**
     * Получает полную информацию о пользователе по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return DTO пользователя
     */
    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable @Valid Long userId) {
        log.info("Internal: запрос пользователя {}", userId);
        return userService.getUserDtoById(userId);
    }

    /**
     * Получает краткую информацию о пользователе по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return краткое DTO
     */
    @GetMapping("/{userId}/short")
    public UserShortDto getUserShortById(@PathVariable @Valid Long userId) {
        log.info("Internal: запрос краткого DTO пользователя {}", userId);
        return userService.getUserShortDtoById(userId);
    }
}