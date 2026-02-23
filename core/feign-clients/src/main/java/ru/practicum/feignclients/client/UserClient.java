package ru.practicum.feignclients.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

/**
 * Feign-клиент для взаимодействия с user-service.
 * Предоставляет методы для получения информации о пользователях.
 */
@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient {

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return DTO пользователя
     */
    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);

    /**
     * Получает список пользователей по их идентификаторам.
     *
     * @param ids список идентификаторов пользователей
     * @return список DTO пользователей
     */
    @GetMapping
    List<UserDto> getUsersByIds(@RequestParam("ids") List<Long> ids);

    /**
     * Получает краткую информацию о пользователе по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return краткое DTO пользователя
     */
    @GetMapping("/{userId}/short")
    UserShortDto getUserShortById(@PathVariable("userId") Long userId);
}