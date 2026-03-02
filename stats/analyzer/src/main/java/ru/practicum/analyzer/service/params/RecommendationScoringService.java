package ru.practicum.analyzer.service.params;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис вычисления итогового скора для рекомендаций.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationScoringService {

    private final EventSimilarityService similarityService;
    private final UserActionService userActionService;

    /**
     * Рассчитывает оценку релевантности события для пользователя.
     *
     * @param eventId ID события-кандидата
     * @param userId  ID пользователя
     * @param limit   максимальное количество похожих событий для учёта
     * @return итоговый скор
     */
    public double calculateRecommendationScore(Long eventId, Long userId, int limit) {
        log.info("Расчёт скора для eventId={}, userId={}", eventId, userId);

        List<EventSimilarity> simA = similarityService.findSimilarByEventA(eventId, limit);
        List<EventSimilarity> simB = similarityService.findSimilarByEventB(eventId, limit);

        Map<Long, Double> similarityScores = new HashMap<>();

        collectViewedSimilarities(simA, true, userId, similarityScores);
        collectViewedSimilarities(simB, false, userId, similarityScores);

        Map<Long, Double> ratings = userActionService.getUserRatingsForEvents(userId, similarityScores.keySet());

        double sumWeighted = 0.0;
        double sumSimilarity = 0.0;

        for (Map.Entry<Long, Double> entry : similarityScores.entrySet()) {
            Double rating = ratings.get(entry.getKey());
            if (rating != null) {
                sumWeighted += rating * entry.getValue();
                sumSimilarity += entry.getValue();
            }
        }

        double score = sumSimilarity > 0 ? sumWeighted / sumSimilarity : 0.0;
        log.info("Итоговый скор: {}", score);
        return score;
    }

    private void collectViewedSimilarities(
            List<EventSimilarity> similarities,
            boolean isEventB,
            Long userId,
            Map<Long, Double> accumulator) {

        for (EventSimilarity es : similarities) {
            Long relatedId = isEventB ? es.getEventB() : es.getEventA();
            if (userActionService.hasUserInteractedWithEvent(userId, relatedId)) {
                accumulator.put(relatedId, es.getScore());
                log.debug("Похожее просмотренное событие: {} со скором {}", relatedId, es.getScore());
            }
        }
    }
}