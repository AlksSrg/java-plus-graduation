package ru.practicum.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для представления данных пользователя.
 * <p>
 * Используется для возврата данных о пользователе в API.
 */
@Builder
@Setter
@Getter
public class UserDto {

    /**
     * Уникальный идентификатор пользователя.
     * <p>
     * Только для чтения.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;

    /**
     * Email пользователя.
     */
    @Email(message = "Некорректный формат email")
    private String email;

    /**
     * Имя пользователя.
     */
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
}