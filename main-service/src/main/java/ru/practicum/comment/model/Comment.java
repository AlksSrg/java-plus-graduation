package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Сущность комментария.
 * Представляет комментарий пользователя к событию в системе.
 * Содержит информацию об авторе, событии, тексте комментария и времени создания.
 */
@Builder(toBuilder = true)
@Table(name = "comments")
@Entity
@Getter
@Setter
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
     * Автор комментария.
     * Связь многие-к-одному с сущностью User.
     * Загружается лениво для оптимизации производительности.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Событие, к которому относится комментарий.
     * Связь многие-к-одному с сущностью Event.
     * Загружается лениво для оптимизации производительности.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /**
     * Дата и время создания комментария.
     * Устанавливается автоматически при создании комментария.
     */
    @Column(nullable = false)
    private LocalDateTime created;

    /**
     * Текст комментария.
     * Должен содержать от 3 до 5000 символов.
     * Не может быть пустым или состоять только из пробелов.
     */
    @Column(nullable = false, length = 5000)
    private String text;

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