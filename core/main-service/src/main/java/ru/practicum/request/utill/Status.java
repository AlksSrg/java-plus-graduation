package ru.practicum.request.utill;

/**
 * Перечисление статусов заявок на участие в событиях.
 */
public enum Status {
    PENDING,    // В ожидании
    CONFIRMED,  // Подтверждено
    CANCELED,   // Отменено
    REJECTED    // Отклонено
}