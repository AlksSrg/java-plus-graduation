package ru.practicum.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO класс для представления информации об ошибке в HTTP ответах.
 */
@Getter
@Setter
@Builder
public class ResponseError {
    private String errorMessage;
    private String reason;
    private String timestamp;
}