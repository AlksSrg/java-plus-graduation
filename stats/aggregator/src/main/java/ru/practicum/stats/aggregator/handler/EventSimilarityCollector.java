package ru.practicum.stats.aggregator.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

/**
 * Собирает и вычисляет схожесть мероприятий.
 */
@Slf4j
@Component
public class EventSimilarityCollector {

    // eventId -> (userId -> max weight)
    private final Map<Long, Map<Long, Double>> eventUserMaxWeight = new HashMap<>();
    private final ObjectMapper objectMapper;

    public EventSimilarityCollector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Обновляет состояние и возвращает новые схожести.
     */
    public List<EventSimilarityAvro> updateState(UserActionAvro action) {
        logAction(action);

        if (!needsUpdate(action)) {
            log.debug("Данные не изменились");
            return Collections.emptyList();
        }

        updateWeight(action);
        return calculateSimilarities(action);
    }

    /**
     * Логирует полученное действие.
     */
    private void logAction(UserActionAvro action) {
        try {
            log.info("Получено действие: {}", objectMapper.writeValueAsString(action));
        } catch (JsonProcessingException e) {
            log.info("Получено действие: {}", action);
        }
    }

    /**
     * Проверяет, нужно ли обновлять данные.
     */
    private boolean needsUpdate(UserActionAvro action) {
        double newWeight = getWeight(action.getActionType());

        Map<Long, Double> userWeights = eventUserMaxWeight.get(action.getEventId());
        if (userWeights != null) {
            Double oldWeight = userWeights.get(action.getUserId());
            if (oldWeight != null && oldWeight >= newWeight) {
                log.debug("Вес {} не превышает текущий {}", newWeight, oldWeight);
                return false;
            }
        }

        log.debug("Требуется обновление: новый вес {}", newWeight);
        return true;
    }

    /**
     * Обновляет максимальный вес для пользователя.
     */
    private void updateWeight(UserActionAvro action) {
        eventUserMaxWeight
                .computeIfAbsent(action.getEventId(), k -> new HashMap<>())
                .put(action.getUserId(), getWeight(action.getActionType()));
    }

    /**
     * Возвращает вес для типа действия.
     */
    private double getWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    /**
     * Вычисляет схожести для мероприятия.
     */
    private List<EventSimilarityAvro> calculateSimilarities(UserActionAvro action) {
        // TODO: Реализовать алгоритм вычисления схожести
        return Collections.emptyList();
    }
}