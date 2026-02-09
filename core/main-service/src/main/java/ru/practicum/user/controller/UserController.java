package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;
import ru.practicum.user.utill.UserGetParam;

import java.util.List;

/**
 * Контроллер для управления пользователями (административный функционал).
 * <p>
 * Предоставляет API для создания, получения и удаления пользователей.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    /**
     * Получает перечень пользователей с возможностью фильтрации по идентификаторам и пагинацией.
     *
     * @param ids  список идентификаторов пользователей для фильтрации (опционально)
     * @param from количество элементов, которые нужно пропустить для формирования текущего набора
     * @param size количество элементов в наборе
     * @return список пользователей
     */
    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0")
                                  @PositiveOrZero(message = "Значение не может быть меньше нуля") int from,
                                  @RequestParam(defaultValue = "10")
                                  @Positive(message = "Значение может быть только положительным") int size) {

        return userService.getUsers(UserGetParam.builder()
                .ids(ids)
                .from(from)
                .size(size)
                .build());
    }

    /**
     * Создает нового пользователя.
     *
     * @param newUserRequest данные нового пользователя
     * @return созданный пользователь
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        return userService.createUser(newUserRequest);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive Long userId) {
        userService.deleteUser(userId);
    }
}