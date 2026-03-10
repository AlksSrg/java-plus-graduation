package ru.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.category.model.Category;

import java.util.Optional;

/**
 * Репозиторий для работы с категориями.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Находит категорию по точному имени (без учета регистра).
     *
     * @param name имя категории
     * @return Optional с категорией
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Находит категорию по точному имени, исключая указанный идентификатор.
     *
     * @param name       имя категории
     * @param excludedId идентификатор, который нужно исключить
     * @return Optional с категорией
     */
    Optional<Category> findByNameIgnoreCaseAndIdNot(String name, Long excludedId);
}