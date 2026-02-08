package ru.practicum.event.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Аннотация для валидации даты события (должна быть не ранее чем через 2 часа от текущего времени).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomFutureValidator.class)
@Target({ElementType.FIELD})
public @interface CustomFuture {
    String message() default "Дата должна быть не ранее текущей + 2 часа";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}