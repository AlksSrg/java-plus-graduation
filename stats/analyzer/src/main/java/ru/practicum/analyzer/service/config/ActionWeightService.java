package ru.practicum.analyzer.service.config;

import ru.practicum.analyzer.enums.ActionType;

/**
 * Интерфейс сервиса весов действий.
 */
public interface ActionWeightService {

    /**
     * Возвращает вес действия.
     *
     * @param actionType тип действия
     * @return числовой вес
     */
    double getWeight(ActionType actionType);
}