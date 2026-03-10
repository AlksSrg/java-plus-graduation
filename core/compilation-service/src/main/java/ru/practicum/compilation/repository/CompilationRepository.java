package ru.practicum.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с подборками событий.
 */
@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    Optional<Compilation> findById(Long id);

    boolean existsByTitle(String title);

    /**
     * Находит идентификаторы подборок с опциональным фильтром по закреплению,
     * с пагинацией и сортировкой.
     *
     * @param pinned   фильтр по закреплению (null — все)
     * @param pageable параметры пагинации (должны включать сортировку)
     * @return список идентификаторов подборок
     */
    @Query("SELECT c.id FROM Compilation c WHERE (:pinned IS NULL OR c.pinned = :pinned) ORDER BY c.id ASC")
    List<Long> findIdsByPinned(@Param("pinned") Boolean pinned, Pageable pageable);

    /**
     * Находит все подборки по списку идентификаторов.
     *
     * @param ids список идентификаторов
     * @return список подборок
     */
    List<Compilation> findAllByIdIn(@Param("ids") List<Long> ids);
}