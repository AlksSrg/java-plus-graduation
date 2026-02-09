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

    static Specification<Comment> byEventId(long eventId) {
        return (root, cq, cb) ->
                cb.equal(root.get("event").get("id"), eventId);
    }

    static Specification<Comment> byAuthor(List<Long> authorIds) {
        return (root, cq, cb) ->
                root.get("author").get("id").in(authorIds);
    }
}