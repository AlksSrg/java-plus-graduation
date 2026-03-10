package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.service.params.EventSimilarityService;
import ru.practicum.analyzer.service.params.RecommendationScoringService;
import ru.practicum.analyzer.service.params.UserActionService;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Основной сервис формирования рекомендаций и статистики.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyzerService {

    private final UserActionService userActionService;
    private final EventSimilarityService similarityService;
    private final RecommendationScoringService scoringService;

    /**
     * Получает персональные рекомендации для пользователя.
     *
     * @param request запрос с ID пользователя и максимальным количеством
     * @return список рекомендаций
     */
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        Long userId = request.getUserId();
        int limit = request.getMaxResults();

        log.info("Запрос рекомендаций для пользователя {}, лимит {}", userId, limit);

        Set<Long> viewed = userActionService.getRecentlyViewedEventIds(userId, limit);
        if (viewed.isEmpty()) {
            log.info("Нет просмотренных событий для пользователя {}", userId);
            return Collections.emptyList();
        }

        Set<Long> candidates = findCandidates(userId, viewed, limit);
        log.info("Найдено кандидатов: {}", candidates.size());

        return generateRecommendations(candidates, userId, limit);
    }

    private Set<Long> findCandidates(Long userId, Set<Long> viewed, int limit) {
        List<EventSimilarity> simA = similarityService.findSimilarByEventAIn(viewed, limit);
        List<EventSimilarity> simB = similarityService.findSimilarByEventBIn(viewed, limit);
        Set<Long> candidates = new HashSet<>();

        addCandidates(simA, true, userId, candidates);
        addCandidates(simB, false, userId, candidates);

        return candidates;
    }

    private void addCandidates(List<EventSimilarity> similarities, boolean isEventB,
                               Long userId, Set<Long> candidates) {
        for (EventSimilarity es : similarities) {
            Long candId = isEventB ? es.getEventB() : es.getEventA();
            if (!userActionService.hasUserInteractedWithEvent(userId, candId)) {
                candidates.add(candId);
            }
        }
    }

    private List<RecommendedEventProto> generateRecommendations(Set<Long> candidates, Long userId, int limit) {
        Map<Long, Double> scores = candidates.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> scoringService.calculateRecommendationScore(id, userId, limit)
                ));

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();
    }

    /**
     * Получает список событий, похожих на заданное.
     *
     * @param request запрос с ID события и пользователя
     * @return список похожих событий с оценками
     */
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Long eventId = request.getEventId();
        Long userId = request.getUserId();
        int limit = request.getMaxResults();

        log.info("Запрос похожих на событие {}, пользователь {}, лимит {}", eventId, userId, limit);

        List<EventSimilarity> simA = similarityService.findSimilarByEventA(eventId, limit);
        List<EventSimilarity> simB = similarityService.findSimilarByEventB(eventId, limit);

        List<RecommendedEventProto> recommendations = new ArrayList<>();
        similarityService.filterAndAddRecommendations(recommendations, simA, true, userId);
        similarityService.filterAndAddRecommendations(recommendations, simB, false, userId);

        recommendations.sort(Comparator.comparing(RecommendedEventProto::getScore).reversed());
        return recommendations.size() > limit ? recommendations.subList(0, limit) : recommendations;
    }

    /**
     * Возвращает суммарное количество взаимодействий для запрошенных событий.
     *
     * @param request запрос со списком ID событий
     * @return статистика (ID события -> вес)
     */
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());
        Map<Long, Double> scores = userActionService.computeEventScores(eventIds);
        return scores.entrySet().stream()
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();
    }
}