package ru.practicum.stats.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

/**
 * Сервис для вычисления схожести мероприятий на основе действий пользователей.
 */
@Slf4j
@Service
public class AggregationService {

    // eventId -> (userId -> максимальный вес)
    private final Map<Long, Map<Long, Double>> eventActions = new HashMap<>();
    // eventId -> сумма всех весов
    private final Map<Long, Double> eventWeightSums = new HashMap<>();
    // (min(eventA,eventB)) -> (max(eventA,eventB) -> сумма минимальных весов для каждого пользователя)
    private final Map<Long, Map<Long, Double>> eventMinWeightSums = new HashMap<>();

    /**
     * Обрабатывает действие пользователя и возвращает список обновлённых схожестей.
     *
     * @param action действие пользователя
     * @return список событий схожести, которые требуется отправить
     */
    public List<EventSimilarityAvro> updateSimilarity(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = toWeight(action.getActionType());

        Map<Long, Double> userActions = eventActions.computeIfAbsent(eventId, k -> new HashMap<>());
        double oldWeight = userActions.getOrDefault(userId, 0.0);

        if (Double.compare(newWeight, oldWeight) <= 0) {
            log.debug("Вес {} не превышает текущий {} для userId={}, eventId={}",
                    newWeight, oldWeight, userId, eventId);
            return Collections.emptyList();
        }

        // Обновляем вес для пользователя и сумму весов события
        updateUserAction(userId, eventId, oldWeight, newWeight, userActions);

        List<EventSimilarityAvro> similarities = new ArrayList<>();
        // Перебираем все другие события, в которых участвовал этот пользователь
        for (Map.Entry<Long, Map<Long, Double>> entry : eventActions.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == eventId) continue;

            double otherWeight = entry.getValue().getOrDefault(userId, 0.0);
            if (otherWeight > 0) {
                double newMinSum = updateMinWeightSums(userId, eventId, otherEventId, oldWeight, newWeight);
                double similarity = calculateSimilarity(eventId, otherEventId, newMinSum);
                similarities.add(createSimilarityAvro(eventId, otherEventId, similarity, action.getTimestamp()));
            }
        }

        return similarities;
    }

    private double toWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private void updateUserAction(long userId, long eventId, double oldWeight, double newWeight,
                                  Map<Long, Double> userActions) {
        double oldSum = eventWeightSums.getOrDefault(eventId, 0.0);
        double newSum = oldSum - oldWeight + newWeight;
        userActions.put(userId, newWeight);
        eventWeightSums.put(eventId, newSum);
        log.debug("Сумма весов для eventId={} обновлена: {} -> {}", eventId, oldSum, newSum);
    }

    private double updateMinWeightSums(long userId, long eventA, long eventB,
                                       double oldWeightA, double newWeightA) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        double weightB = eventActions.getOrDefault(second, Collections.emptyMap())
                .getOrDefault(userId, 0.0);
        if (weightB == 0.0) return 0.0;

        Map<Long, Double> minWeights = eventMinWeightSums.computeIfAbsent(first, k -> new HashMap<>());
        double oldSum = minWeights.getOrDefault(second, 0.0);

        double oldMin = Math.min(oldWeightA, weightB);
        double newMin = Math.min(newWeightA, weightB);
        double newSum = oldSum - oldMin + newMin;
        minWeights.put(second, newSum);

        return newSum;
    }

    private double calculateSimilarity(long eventA, long eventB, double minSum) {
        double sumA = eventWeightSums.getOrDefault(eventA, 0.0);
        double sumB = eventWeightSums.getOrDefault(eventB, 0.0);
        if (sumA <= 0 || sumB <= 0) return 0.0;
        return minSum / Math.sqrt(sumA * sumB);
    }

    private EventSimilarityAvro createSimilarityAvro(long eventA, long eventB, double score, Instant timestamp) {
        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(eventA, eventB))
                .setEventB(Math.max(eventA, eventB))
                .setScore(score)
                .setTimestamp(timestamp)
                .build();
    }
}