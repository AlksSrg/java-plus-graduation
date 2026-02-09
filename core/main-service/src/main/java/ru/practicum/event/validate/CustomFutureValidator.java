package ru.practicum.event.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

/**
 * Валидатор для проверки даты события.
 */
public class CustomFutureValidator implements ConstraintValidator<CustomFuture, LocalDateTime> {

    @Override
    public void initialize(CustomFuture constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext constraintValidatorContext) {
        return localDateTime == null ? true : localDateTime.isAfter(LocalDateTime.now().plusHours(2));
    }
}