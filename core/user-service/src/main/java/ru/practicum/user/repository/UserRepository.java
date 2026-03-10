package ru.practicum.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по точному email (регистронезависимо).
     *
     * @param email email
     * @return Optional с пользователем
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Проверяет существование пользователя с указанным email.
     *
     * @param email email
     * @return true, если существует
     */
    boolean existsByEmail(String email);

    /**
     * Находит всех пользователей с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return список пользователей
     */
    @Query("SELECT u FROM User u")
    List<User> findAllWithPagination(Pageable pageable);

    /**
     * Находит пользователей по списку идентификаторов с пагинацией.
     *
     * @param ids      список идентификаторов
     * @param pageable параметры пагинации
     * @return список пользователей
     */
    List<User> findByIdIn(List<Long> ids, Pageable pageable);
}