package ru.practicum.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.List;

/**
 * Сервис для работы с пользователями.
 */
public interface UserService {

    /**
     * Создаёт нового пользователя.
     *
     * @param request данные для создания
     * @return созданный пользователь
     */
    UserDto createUser(NewUserRequest request);

    /**
     * Получает список пользователей с фильтрацией по идентификаторам и пагинацией.
     *
     * @param ids      список идентификаторов (если null или пусто – все пользователи)
     * @param pageable параметры пагинации
     * @return список пользователей
     */
    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор
     */
    void deleteUser(Long userId);

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор
     * @return сущность пользователя
     */
    User getUserById(Long userId);

    /**
     * Получает пользователя по идентификатору или выбрасывает исключение.
     *
     * @param userId идентификатор
     * @return сущность пользователя
     */
    User getUserByIdOrThrow(Long userId);

    /**
     * Получает DTO пользователя по идентификатору.
     *
     * @param userId идентификатор
     * @return DTO пользователя
     */
    UserDto getUserDtoById(Long userId);

    /**
     * Получает краткое DTO пользователя по идентификатору.
     *
     * @param userId идентификатор
     * @return краткое DTO
     */
    UserShortDto getUserShortDtoById(Long userId);
}