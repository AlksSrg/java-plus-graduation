package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.service.params.EventSimilarityService;
import ru.practicum.analyzer.service.params.RecommendationScoringService;
import ru.practicum.analyzer.service.params.UserActionService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserRecommendationsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Основной сервис формирования рекомендаций.
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
     * @param request запрос с ID пользователя
     * @return список рекомендаций
     */
    public List<RecommendedEventProto> getRecommendationsForUser(UserRecommendationsRequestProto request) {
        Long userId = request.getUserId();
        int limit = request.getMaxResults();

        log.info("Получение рекомендаций для пользователя: {}, лимит: {}", userId, limit);

        Set<Long> viewedEvents = userActionService.getRecentlyViewedEventIds(userId, limit);
        if (viewedEvents.isEmpty()) {
            log.info("Нет просмотренных событий для пользователя {}", userId);
            return Collections.emptyList();
        }

        Set<Long> candidates = findCandidateRecommendations(userId, viewedEvents, limit);
        log.info("Найдено кандидатов: {}", candidates.size());

        return generateRecommendations(candidates, userId, limit);
    }

    /**
     * Находит кандидатов для рекомендаций.
     */
    private Set<Long> findCandidateRecommendations(Long userId, Set<Long> viewedEvents, int limit) {
        List<EventSimilarity> simA = similarityService.findSimilarByEventAIn(viewedEvents, limit);
        List<EventSimilarity> simB = similarityService.findSimilarByEventBIn(viewedEvents, limit);

        Set<Long> candidates = new HashSet<>();
        addCandidates(simA, true, userId, candidates);
        addCandidates(simB, false, userId, candidates);

        return candidates;
    }

    /**
     * Добавляет кандидатов из списка схожести.
     */
    private void addCandidates(List<EventSimilarity> similarities, boolean isEventB,
                               Long userId, Set<Long> candidates) {
        for (EventSimilarity es : similarities) {
            Long candidateId = isEventB ? es.getEventB() : es.getEventA();
            if (!userActionService.hasUserInteractedWithEvent(userId, candidateId)) {
                candidates.add(candidateId);
            }
        }
    }

    /**
     * Генерирует рекомендации с оценками.
     */
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
     * Находит похожие мероприятия.
     *
     * @param request запрос с ID мероприятия
     * @return список похожих мероприятий
     */
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Long eventId = request.getEventId();
        Long userId = request.getUserId();
        int limit = request.getMaxResults();

        log.info("Поиск похожих на событие: {}, пользователь: {}, лимит: {}", eventId, userId, limit);

        List<EventSimilarity> simA = similarityService.findSimilarByEventA(eventId, limit);
        List<EventSimilarity> simB = similarityService.findSimilarByEventB(eventId, limit);

        List<RecommendedEventProto> recommendations = new ArrayList<>();

        similarityService.filterAndAddRecommendations(recommendations, simA, true, userId);
        similarityService.filterAndAddRecommendations(recommendations, simB, false, userId);

        recommendations.sort(Comparator.comparing(RecommendedEventProto::getScore).reversed());
        return recommendations.size() > limit ? recommendations.subList(0, limit) : recommendations;
    }

    /**
     * Получает статистику взаимодействий.
     *
     * @param request запрос со списком мероприятий
     * @return статистика по мероприятиям
     */
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());
        log.info("Получение статистики для {} событий", eventIds.size());

        Map<Long, Double> scores = userActionService.computeEventScores(eventIds);

        return scores.entrySet().stream()
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();
    }
}