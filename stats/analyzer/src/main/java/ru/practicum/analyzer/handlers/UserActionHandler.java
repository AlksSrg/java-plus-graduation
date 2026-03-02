package ru.practicum.analyzer.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.enums.ActionType;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.UserActionRepository;
import ru.practicum.analyzer.service.config.ActionWeightService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Optional;

/**
 * Обработчик действий пользователей, сохраняет или обновляет их в БД.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionHandler {

    private final UserActionRepository userActionRepository;
    private final ActionWeightService weightService;

    /**
     * Обрабатывает полученное действие пользователя.
     *
     * @param avro действие в формате Avro
     */
    @Transactional
    public void handle(UserActionAvro avro) {
        log.info("Обработка действия: userId={}, eventId={}, action={}",
                avro.getUserId(), avro.getEventId(), avro.getActionType());

        Optional<UserAction> existing = userActionRepository.findByUserIdAndEventId(
                avro.getUserId(), avro.getEventId());

        if (existing.isPresent()) {
            updateIfWeightHigher(existing.get(), avro);
        } else {
            createNew(avro);
        }
    }

    private void updateIfWeightHigher(UserAction action, UserActionAvro avro) {
        double currentWeight = weightService.getWeight(action.getActionType());
        double newWeight = weightService.getWeight(ActionType.valueOf(avro.getActionType().name()));

        if (newWeight > currentWeight) {
            action.setActionType(ActionType.valueOf(avro.getActionType().name()));
            action.setTimestamp(avro.getTimestamp());
            userActionRepository.save(action);
            log.info("Действие обновлено (вес увеличен)");
        } else {
            log.debug("Пропуск обновления: новый вес {} <= текущего {}", newWeight, currentWeight);
        }
    }

    private void createNew(UserActionAvro avro) {
        UserAction action = UserAction.builder()
                .userId(avro.getUserId())
                .eventId(avro.getEventId())
                .actionType(ActionType.valueOf(avro.getActionType().name()))
                .timestamp(avro.getTimestamp())
                .build();
        userActionRepository.save(action);
        log.info("Новое действие сохранено");
    }
}