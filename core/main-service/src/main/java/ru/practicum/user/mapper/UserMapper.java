package ru.practicum.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

/**
 * Утилитарный класс для преобразования между сущностью User и DTO.
 * <p>
 * Предоставляет статические методы для маппинга объектов.
 */
@UtilityClass
public class UserMapper {

    /**
     * Преобразует сущность User в UserDto.
     *
     * @param user сущность пользователя
     * @return DTO пользователя
     */
    public static UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    /**
     * Преобразует NewUserRequest в сущность User.
     *
     * @param newUserRequest DTO для создания пользователя
     * @return сущность пользователя
     */
    public static User mapToUser(NewUserRequest newUserRequest) {
        return User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }

    /**
     * Преобразует UserDto в сущность User.
     *
     * @param userDto DTO пользователя
     * @return сущность пользователя
     */
    public static User mapFromDto(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    /**
     * Преобразует сущность User в UserShortDto.
     *
     * @param user сущность пользователя
     * @return краткое DTO пользователя
     */
    public static UserShortDto mapToUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}