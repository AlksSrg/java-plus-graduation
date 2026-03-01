package ru.practicum.analyzer.service.config;

import org.springframework.stereotype.Component;
import ru.practicum.analyzer.enums.ActionType;

import java.util.Map;

/**
 * Реализация сервиса весов по умолчанию.
 */
@Component
public class DefaultActionWeightService implements ActionWeightService {

    private static final Map<ActionType, Double> WEIGHTS = Map.of(
            ActionType.VIEW, 0.4,
            ActionType.REGISTER, 0.8,
            ActionType.LIKE, 1.0
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public double getWeight(ActionType actionType) {
        return WEIGHTS.getOrDefault(actionType, 0.0);
    }
}