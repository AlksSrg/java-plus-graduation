package ru.practicum.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью User.
 * <p>
 * Предоставляет методы для выполнения операций с пользователями в базе данных.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по email (регистронезависимо).
     *
     * @param email email пользователя
     * @return Optional с пользователем, если найден
     */
    Optional<User> findByEmailContainingIgnoreCase(String email);

    /**
     * Находит пользователей по списку идентификаторов.
     *
     * @param ids список идентификаторов пользователей
     * @return список пользователей
     */
    List<User> findAllByIdIn(List<Long> ids);
}