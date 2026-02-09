package ru.practicum.category.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность категории.
 */
@Builder
@Table(name = "categories")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}