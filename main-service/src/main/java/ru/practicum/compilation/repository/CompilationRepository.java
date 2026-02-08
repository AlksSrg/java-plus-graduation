package ru.practicum.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.Optional;

/**
 * Репозиторий для работы с подборками событий.
 */
@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    /**
     * Находит подборку по идентификатору.
     *
     * @param id идентификатор подборки
     * @return подборка, если найдена
     */
    Optional<Compilation> findById(Long id);

    /**
     * Находит все подборки с указанным значением закрепления.
     *
     * @param pinned   признак закрепления подборки
     * @param pageable параметры пагинации
     * @return страница подборок
     */
    Page<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

    /**
     * Проверяет существование подборки с указанным названием.
     *
     * @param title название подборки
     * @return true если подборка с таким названием существует
     */
    boolean existsByTitle(String title);

    /**
     * Находит все подборки с опциональным фильтром по закреплению.
     *
     * @param pinned   признак закрепления подборки (может быть null)
     * @param pageable параметры пагинации
     * @return страница подборок
     */
    @Query("SELECT c FROM Compilation c WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    Page<Compilation> findAllByPinnedOptional(@Param("pinned") Boolean pinned, Pageable pageable);
}