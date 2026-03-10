package ru.practicum.user.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность пользователя.
 * Представляет пользователя системы с базовой информацией.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Имя пользователя.
     */
    @Column(nullable = false, length = 250)
    private String name;

    /**
     * Email пользователя.
     * Должен быть уникальным.
     */
    @Column(nullable = false, length = 254, unique = true)
    private String email;
}