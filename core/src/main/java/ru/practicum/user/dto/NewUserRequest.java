package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для создания нового пользователя.
 * <p>
 * Содержит данные, необходимые для регистрации пользователя.
 */
@Builder
@Getter
@Setter
public class NewUserRequest {

    /**
     * Email пользователя.
     * <p>
     * Должен быть уникальным, непустым и соответствовать формату email.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Size(min = 6, max = 254, message = "Email должен содержать от 6 до 254 символов")
    @Email(message = "Некорректный формат email")
    private String email;

    /**
     * Имя пользователя.
     * <p>
     * Должно быть непустым и содержать от 2 до 250 символов.
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250, message = "Имя должно содержать от 2 до 250 символов")
    private String name;
}