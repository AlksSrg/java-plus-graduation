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
 * Сервис скоринга рекомендаций.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationScoringService {

    private final EventSimilarityService similarityService;
    private final UserActionService userActionService;

    /**
     * Рассчитывает оценку релевантности.
     *
     * @param eventId ID мероприятия
     * @param userId  ID пользователя
     * @param limit   лимит
     * @return оценка релевантности
     */
    public double calculateRecommendationScore(Long eventId, Long userId, int limit) {
        List<EventSimilarity> simA = similarityService.findSimilarByEventA(eventId, limit);
        List<EventSimilarity> simB = similarityService.findSimilarByEventB(eventId, limit);

        Map<Long, Double> similarityScores = new HashMap<>();

        collectSimilarities(simA, true, userId, similarityScores);
        collectSimilarities(simB, false, userId, similarityScores);

        Map<Long, Double> ratings = userActionService.getUserRatingsForEvents(userId, similarityScores.keySet());

        double sumWeighted = 0;
        double sumSimilarity = 0;

        for (Map.Entry<Long, Double> entry : similarityScores.entrySet()) {
            Double rating = ratings.get(entry.getKey());
            if (rating != null) {
                sumWeighted += rating * entry.getValue();
                sumSimilarity += entry.getValue();
            }
        }

        return sumSimilarity > 0 ? sumWeighted / sumSimilarity : 0;
    }

    /**
     * Собирает информацию о просмотренных похожих мероприятиях.
     */
    private void collectSimilarities(
            List<EventSimilarity> similarities,
            boolean isEventB,
            Long userId,
            Map<Long, Double> result) {

        for (EventSimilarity es : similarities) {
            Long relatedId = isEventB ? es.getEventB() : es.getEventA();
            if (userActionService.hasUserInteractedWithEvent(userId, relatedId)) {
                result.put(relatedId, es.getScore());
            }
        }
    }
}