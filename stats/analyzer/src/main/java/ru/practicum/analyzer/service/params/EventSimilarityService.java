package ru.practicum.analyzer.service.params;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;

import java.util.List;
import java.util.Set;

/**
 * Сервис для работы со схожестью мероприятий (чтение из БД).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSimilarityService {

    private final EventSimilarityRepository repository;
    private final UserActionService userActionService;

    public List<EventSimilarity> findSimilarByEventA(Long eventId, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return repository.findAllByEventA(eventId, page);
    }

    public List<EventSimilarity> findSimilarByEventB(Long eventId, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return repository.findAllByEventB(eventId, page);
    }

    public List<EventSimilarity> findSimilarByEventAIn(Set<Long> eventIds, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return repository.findAllByEventAIn(eventIds, page);
    }

    public List<EventSimilarity> findSimilarByEventBIn(Set<Long> eventIds, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return repository.findAllByEventBIn(eventIds, page);
    }

    /**
     * Фильтрует похожие события, исключая те, с которыми пользователь уже взаимодействовал,
     * и добавляет их в список рекомендаций.
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
                log.debug("Добавлен кандидат: {}, score={}", candidateId, es.getScore());
            }
        }
    }
}