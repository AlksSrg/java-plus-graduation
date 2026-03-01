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
 * Сервис для работы с действиями пользователей.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActionService {

    private final UserActionRepository userActionRepository;
    private final ActionWeightService actionWeightService;

    /**
     * Возвращает ID недавно просмотренных мероприятий.
     *
     * @param userId ID пользователя
     * @param limit  лимит
     * @return набор ID мероприятий
     */
    public Set<Long> getRecentlyViewedEventIds(Long userId, int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return userActionRepository.findAllByUserId(userId, page).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());
    }

    /**
     * Проверяет, взаимодействовал ли пользователь с мероприятием.
     *
     * @param userId  ID пользователя
     * @param eventId ID мероприятия
     * @return true если взаимодействовал
     */
    public boolean hasUserInteractedWithEvent(Long userId, Long eventId) {
        return userActionRepository.existsByEventIdAndUserId(eventId, userId);
    }

    /**
     * Возвращает оценки пользователя для мероприятий.
     *
     * @param userId   ID пользователя
     * @param eventIds набор ID мероприятий
     * @return карта оценок
     */
    public Map<Long, Double> getUserRatingsForEvents(Long userId, Set<Long> eventIds) {
        return userActionRepository.findAllByEventIdInAndUserId(eventIds, userId).stream()
                .collect(Collectors.toMap(
                        UserAction::getEventId,
                        a -> actionWeightService.getWeight(a.getActionType())
                ));
    }

    /**
     * Вычисляет суммарные оценки для мероприятий.
     *
     * @param eventIds набор ID мероприятий
     * @return карта суммарных оценок
     */
    public Map<Long, Double> computeEventScores(Set<Long> eventIds) {
        Map<Long, Double> scores = new HashMap<>();

        userActionRepository.findAllByEventIdIn(eventIds).forEach(action -> {
            double weight = actionWeightService.getWeight(action.getActionType());
            scores.merge(action.getEventId(), weight, Double::sum);
        });

        return scores;
    }
}