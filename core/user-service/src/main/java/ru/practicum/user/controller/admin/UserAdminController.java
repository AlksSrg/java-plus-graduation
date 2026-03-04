package ru.practicum.user.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.service.UserService;

import java.util.List;

/**
 * Контроллер для административных операций с пользователями.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    /**
     * Получает список пользователей с фильтрацией по идентификаторам и пагинацией.
     *
     * @param ids  список идентификаторов (опционально)
     * @param from начальная позиция
     * @param size размер страницы
     * @return список пользователей
     */
    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                  @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /admin/users with ids={}, from={}, size={}", ids, from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        return userService.getUsers(ids, pageable);
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор
     * @return DTO пользователя
     */
    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable @Positive Long userId) {
        log.info("GET /admin/users/{}", userId);
        return userService.getUserDtoById(userId);
    }

    /**
     * Создаёт нового пользователя.
     *
     * @param request данные для создания
     * @return созданный пользователь
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest request) {
        log.info("POST /admin/users with body: {}", request);
        return userService.createUser(request);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive Long userId) {
        log.info("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
    }

    /**
     * Получает краткую информацию о пользователе по идентификатору (для внутренних сервисов).
     *
     * @param userId идентификатор
     * @return краткое DTO пользователя
     */
    @GetMapping("/{userId}/short")
    public UserShortDto getUserShortById(@PathVariable @Positive Long userId) {
        log.info("Admin: запрос краткого DTO пользователя {}", userId);
        return userService.getUserShortDtoById(userId);
    }
}