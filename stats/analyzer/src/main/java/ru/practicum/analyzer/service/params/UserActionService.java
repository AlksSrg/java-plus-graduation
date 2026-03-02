package ru.practicum.analyzer.service.params;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.UserActionRepository;
import ru.practicum.analyzer.service.config.ActionWeightService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для работы с действиями пользователей (чтение из БД).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActionService {

    private final UserActionRepository repository;
    private final ActionWeightService weightService;

    /**
     * Возвращает идентификаторы событий, с которыми пользователь взаимодействовал недавно.
     *
     * @param userId идентификатор пользователя
     * @param limit  максимальное количество
     * @return множество идентификаторов событий
     */
    public Set<Long> getRecentlyViewedEventIds(Long userId, int limit) {
        log.info("Поиск последних {} действий пользователя {}", limit, userId);
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return repository.findAllByUserId(userId, page).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());
    }

    /**
     * Проверяет, взаимодействовал ли пользователь с указанным событием.
     */
    public boolean hasUserInteractedWithEvent(Long userId, Long eventId) {
        return repository.existsByEventIdAndUserId(eventId, userId);
    }

    /**
     * Возвращает оценки пользователя для заданных событий (вес действия).
     */
    public Map<Long, Double> getUserRatingsForEvents(Long userId, Set<Long> eventIds) {
        log.info("Получение оценок пользователя {} для {} событий", userId, eventIds.size());
        return repository.findAllByEventIdInAndUserId(eventIds, userId).stream()
                .collect(Collectors.toMap(
                        UserAction::getEventId,
                        action -> weightService.getWeight(action.getActionType())
                ));
    }

    /**
     * Вычисляет суммарные баллы для каждого события на основе действий всех пользователей.
     */
    public Map<Long, Double> computeEventScores(Set<Long> eventIds) {
        log.info("Вычисление суммарных баллов для {} событий", eventIds.size());
        Map<Long, Double> scores = new HashMap<>();
        repository.findAllByEventIdIn(eventIds).forEach(action -> {
            double weight = weightService.getWeight(action.getActionType());
            scores.merge(action.getEventId(), weight, Double::sum);
        });
        return scores;
    }
}