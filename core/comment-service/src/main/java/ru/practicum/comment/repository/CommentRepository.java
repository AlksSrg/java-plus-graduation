package ru.practicum.comment.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.model.Comment;

import java.util.List;

/**
 * Репозиторий для работы с комментариями в базе данных.
 * Предоставляет методы для доступа и управления данными комментариев.
 * Наследует стандартные CRUD-операции от JpaRepository.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    /**
     * Проверяет существование комментария по идентификаторам автора и события.
     *
     * @param authorId идентификатор автора комментария
     * @param eventId  идентификатор события
     * @return true если комментарий существует, иначе false
     */
    boolean existsByAuthorIdAndEventId(long authorId, long eventId);

    /**
     * Находит все комментарии указанного автора.
     *
     * @param authorId идентификатор автора
     * @return список комментариев автора, отсортированный по дате создания
     */
    List<Comment> findAllByAuthorId(long authorId);

    /**
     * Находит все комментарии для указанного события.
     *
     * @param eventId идентификатор события
     * @return список комментариев события, отсортированный по дате создания
     */
    List<Comment> findAllByEventId(long eventId);

    /**
     * Удаляет все комментарии указанного автора.
     *
     * @param authorId идентификатор автора
     */
    void deleteAllByAuthorId(long authorId);

    /**
     * Удаляет все комментарии для указанного события.
     *
     * @param eventId идентификатор события
     */
    void deleteAllByEventId(long eventId);

    /**
     * Подсчитывает количество комментариев для события.
     *
     * @param eventId идентификатор события
     * @return количество комментариев
     */
    long countByEventId(long eventId);

    /**
     * Спецификация для фильтрации комментариев по ID события.
     *
     * @param eventId идентификатор события
     * @return спецификация для JPA
     */
    static Specification<Comment> byEventId(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("eventId"), eventId);
    }

    /**
     * Спецификация для фильтрации комментариев по списку ID авторов.
     *
     * @param authorIds список идентификаторов авторов
     * @return спецификация для JPA
     */
    static Specification<Comment> byAuthorIds(List<Long> authorIds) {
        return (root, query, cb) -> root.get("authorId").in(authorIds);
    }
}