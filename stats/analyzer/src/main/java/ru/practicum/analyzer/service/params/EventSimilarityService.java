package ru.practicum.analyzer.service.params;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.util.List;
import java.util.Set;

/**
 * Сервис для работы со схожестью мероприятий.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSimilarityService {

    private final EventSimilarityRepository similarityRepository;
    private final UserActionService userActionService;

    /**
     * Находит похожие по eventA.
     *
     * @param eventId ID мероприятия
     * @param limit   лимит
     * @return список записей схожести
     */
    public List<EventSimilarity> findSimilarByEventA(Long eventId, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return similarityRepository.findAllByEventA(eventId, page);
    }

    /**
     * Находит похожие по eventB.
     *
     * @param eventId ID мероприятия
     * @param limit   лимит
     * @return список записей схожести
     */
    public List<EventSimilarity> findSimilarByEventB(Long eventId, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return similarityRepository.findAllByEventB(eventId, page);
    }

    /**
     * Находит похожие по набору eventA.
     *
     * @param eventIds набор ID
     * @param limit    лимит
     * @return список записей схожести
     */
    public List<EventSimilarity> findSimilarByEventAIn(Set<Long> eventIds, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return similarityRepository.findAllByEventAIn(eventIds, page);
    }

    /**
     * Находит похожие по набору eventB.
     *
     * @param eventIds набор ID
     * @param limit    лимит
     * @return список записей схожести
     */
    public List<EventSimilarity> findSimilarByEventBIn(Set<Long> eventIds, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return similarityRepository.findAllByEventBIn(eventIds, page);
    }

    /**
     * Фильтрует и добавляет рекомендации.
     *
     * @param recommendations список для добавления
     * @param similarities    записи схожести
     * @param isEventB        флаг поиска по eventB
     * @param userId          ID пользователя
     */
    public void filterAndAddRecommendations(
            List<RecommendedEventProto> recommendations,
            List<EventSimilarity> similarities,
            boolean isEventB,
            Long userId) {

        for (EventSimilarity es : similarities) {
            Long candidateId = isEventB ? es.getEventB() : es.getEventA();
            if (!userActionService.hasUserInteractedWithEvent(userId, candidateId)) {
                recommendations.add(RecommendedEventProto.newBuilder()
                        .setEventId(candidateId)
                        .setScore(es.getScore())
                        .build());
                log.debug("Добавлен кандидат: {}, score: {}", candidateId, es.getScore());
            }
        }
    }
}