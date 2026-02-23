package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.comment.util.SortOrder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность комментария.
 * Представляет комментарий пользователя к событию в системе.
 * Хранит только ID связанных сущностей для слабой связанности.
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    /**
     * Уникальный идентификатор комментария.
     * Генерируется автоматически при сохранении в базу данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID автора комментария.
     * Вместо прямой ссылки на User храним только ID для слабой связанности.
     */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /**
     * ID события, к которому относится комментарий.
     * Вместо прямой ссылки на Event храним только ID для слабой связанности.
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /**
     * Дата и время создания комментария.
     * Устанавливается автоматически при создании комментария.
     */
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    /**
     * Текст комментария.
     * Должен содержать от 3 до 5000 символов.
     * Не может быть пустым или состоять только из пробелов.
     */
    @Column(nullable = false, length = 5000)
    private String text;

    /**
     * Сортировка комментариев (для запросов).
     * Не сохраняется в БД, используется только для параметров запроса.
     */
    @Transient
    private SortOrder sort;

    /**
     * Сравнивает данный комментарий с другим объектом на равенство.
     * Два комментария считаются равными, если их идентификаторы совпадают.
     *
     * @param o объект для сравнения
     * @return true если объекты равны, иначе false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    /**
     * Возвращает хэш-код комментария на основе его идентификатора.
     *
     * @return хэш-код комментария
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}