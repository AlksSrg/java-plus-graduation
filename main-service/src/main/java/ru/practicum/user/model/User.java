package ru.practicum.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * Сущность пользователя.
 * <p>
 * Представляет пользователя системы с базовой информацией.
 */
@Builder
@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя.
     */
    private String name;

    /**
     * Email пользователя.
     * <p>
     * Должен быть уникальным.
     */
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}