package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

/**
 * Маппер для преобразования между сущностью User и DTO.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Преобразует NewUserRequest в сущность User.
     *
     * @param request DTO для создания
     * @return сущность пользователя
     */
    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest request);

    /**
     * Преобразует сущность User в UserDto.
     *
     * @param user сущность
     * @return DTO пользователя
     */
    UserDto toDto(User user);

    /**
     * Преобразует сущность User в UserShortDto.
     *
     * @param user сущность
     * @return краткое DTO пользователя
     */
    UserShortDto toShortDto(User user);
}