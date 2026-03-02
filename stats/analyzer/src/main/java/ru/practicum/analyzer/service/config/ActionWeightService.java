package ru.practicum.analyzer.service.config;

import ru.practicum.analyzer.enums.ActionType;

/**
 * Сервис для получения веса действия пользователя.
 */
public interface ActionWeightService {

    /**
     * Возвращает числовой вес для указанного типа действия.
     *
     * @param actionType тип действия
     * @return вес (0.0, если тип не поддерживается)
     */
    double getWeight(ActionType actionType);
}