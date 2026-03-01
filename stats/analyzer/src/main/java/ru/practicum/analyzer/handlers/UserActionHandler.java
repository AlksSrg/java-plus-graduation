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
 * Обработчик действий пользователей.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionHandler {

    private final UserActionRepository userActionRepository;
    private final ActionWeightService actionWeightService;

    /**
     * Обрабатывает и сохраняет действие пользователя.
     *
     * @param userActionAvro объект с действием пользователя
     */
    @Transactional
    public void handle(UserActionAvro userActionAvro) {
        log.info("Обработка действия: userId={}, eventId={}, action={}",
                userActionAvro.getUserId(), userActionAvro.getEventId(), userActionAvro.getActionType());

        Optional<UserAction> existing = userActionRepository.findByUserIdAndEventId(
                userActionAvro.getUserId(), userActionAvro.getEventId());

        if (existing.isPresent()) {
            updateExistingAction(existing.get(), userActionAvro);
        } else {
            createNewAction(userActionAvro);
        }
    }

    /**
     * Обновляет существующее действие.
     */
    private void updateExistingAction(UserAction existing, UserActionAvro avro) {
        Double currentWeight = actionWeightService.getWeight(existing.getActionType());
        Double newWeight = actionWeightService.getWeight(ActionType.valueOf(avro.getActionType().name()));

        if (newWeight > currentWeight) {
            existing.setActionType(ActionType.valueOf(avro.getActionType().name()));
            existing.setTimestamp(avro.getTimestamp());
            userActionRepository.save(existing);
            log.info("Действие обновлено");
        }
    }

    /**
     * Создает новое действие.
     */
    private void createNewAction(UserActionAvro avro) {
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