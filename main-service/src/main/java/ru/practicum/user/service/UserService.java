package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.user.utill.UserGetParam;

import java.util.List;

/**
 * Сервис для работы с пользователями.
 * <p>
 * Определяет контракт для операций управления пользователями.
 */
public interface UserService {

    /**
     * Получает перечень пользователей с учетом параметров фильтрации и пагинации.
     *
     * @param userGetParam параметры запроса (фильтрация и пагинация)
     * @return список DTO пользователей
     */
    List<UserDto> getUsers(UserGetParam userGetParam);

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return сущность пользователя
     */
    User getUserById(Long userId);

    /**
     * Создает нового пользователя.
     *
     * @param newUserRequest данные для создания пользователя
     * @return DTO созданного пользователя
     */
    UserDto createUser(NewUserRequest newUserRequest);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    void deleteUser(Long userId);
}