package ru.practicum.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.exception.ValidationException;
import ru.practicum.feignclients.client.EventClient;
import ru.practicum.feignclients.client.UserClient;
import ru.practicum.request.dto.EventWithCountConfirmedRequests;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.service.RequestService;
import ru.practicum.request.util.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с запросами на участие.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestMapper requestMapper;

    // ========== Пользовательские методы ==========

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        log.info("Получение запросов пользователя {}", userId);
        checkUserExists(userId);
        return requestMapper.toDtoList(requestRepository.findByRequesterId(userId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса: пользователь {}, событие {}", userId, eventId);

        checkUserExists(userId);
        EventFullDto event = eventClient.getEventById(eventId);

        // Проверка, что пользователь не является инициатором события
        if (event.initiator().getId().equals(userId)) {
            throw new ConflictResource("Инициатор события не может подать заявку на участие в своём событии");
        }
        if (!"PUBLISHED".equals(event.state().name())) {
            throw new ConflictResource("Нельзя участвовать в неопубликованном событии");
        }
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictResource("Заявка на участие в этом событии уже существует");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (event.participantLimit() > 0 && confirmedCount >= event.participantLimit()) {
            throw new ConflictResource("Достигнут лимит участников для этого события");
        }

        Status status = Status.PENDING;
        if (!event.requestModeration() || event.participantLimit() == 0) {
            status = Status.CONFIRMED;
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .eventId(eventId)
                .requesterId(userId)
                .status(status)
                .build();

        Request saved = requestRepository.save(request);
        log.info("Запрос создан с id: {}", saved.getId());
        return requestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса: пользователь {}, запрос {}", userId, requestId);

        checkUserExists(userId);
        Request request = getRequestOrThrow(requestId);

        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundResource("Запрос с id " + requestId + " не принадлежит пользователю " + userId);
        }
        if (request.getStatus() != Status.PENDING) {
            throw new ConflictResource("Можно отменить только заявки в статусе PENDING");
        }

        request.cancel();
        Request updated = requestRepository.save(request);
        log.info("Запрос {} отменён", requestId);
        return requestMapper.toDto(updated);
    }

    // ========== Внутренние методы для других сервисов ==========

    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(long eventId) {
        log.info("Получение запросов для события {}", eventId);
        return requestMapper.toDtoList(requestRepository.findByEventId(eventId));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByIds(List<Long> ids) {
        log.info("Получение запросов по ids: {}", ids);
        return requestMapper.toDtoList(requestRepository.findAllByIdIn(ids));
    }

    @Override
    public Long countConfirmedRequestsByEventId(long eventId) {
        log.info("Подсчёт подтверждённых запросов для события {}", eventId);
        return requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
    }

    @Override
    public Map<Long, Long> countConfirmedRequestsByEventIds(List<Long> eventIds) {
        log.info("Подсчёт подтверждённых запросов для событий: {}", eventIds);
        List<EventWithCountConfirmedRequests> results =
                requestRepository.countByEventIdInAndStatus(eventIds, Status.CONFIRMED);
        Map<Long, Long> counts = results.stream()
                .collect(Collectors.toMap(EventWithCountConfirmedRequests::getEventId,
                        EventWithCountConfirmedRequests::getCount));
        eventIds.forEach(id -> counts.putIfAbsent(id, 0L));
        return counts;
    }

    @Override
    @Transactional
    public ParticipationRequestDto updateRequestStatus(long requestId, Status status) {
        log.info("Обновление статуса запроса {} на {}", requestId, status);

        Request request = getRequestOrThrow(requestId);

        // Проверка возможности изменения статуса
        validateStatusTransition(request.getStatus(), status);

        request.setStatus(status);
        Request updated = requestRepository.save(request);
        return requestMapper.toDto(updated);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest request) {
        log.info("Массовое обновление статусов: userId={}, eventId={}, request={}", userId, eventId, request);

        EventFullDto event = eventClient.getEventById(eventId);
        // Проверка, что пользователь является инициатором события
        if (!event.initiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является инициатором события");
        }
        if (!event.requestModeration() || event.participantLimit() == 0) {
            throw new ValidationException("Для данного события подтверждение заявок не требуется");
        }

        Status newStatus = request.getStatus();
        if (newStatus == Status.PENDING) {
            throw new ValidationException("Можно устанавливать только статусы CONFIRMED или REJECTED");
        }

        List<Request> requestsForUpdate = requestRepository.findAllByIdIn(request.getRequestIds());
        validateRequestsExist(request.getRequestIds(), requestsForUpdate);
        validateRequestsBelongToEvent(requestsForUpdate, eventId);
        validateRequestsArePending(requestsForUpdate);

        int currentConfirmed = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED).intValue();
        int availableSlots = event.participantLimit() - currentConfirmed;

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        if (newStatus == Status.CONFIRMED) {
            if (availableSlots <= 0) {
                throw new ConflictResource("Нет свободных мест для подтверждения");
            }

            int confirmedCount = 0;
            for (Request req : requestsForUpdate) {
                if (confirmedCount < availableSlots) {
                    req.confirm();
                    confirmed.add(requestMapper.toDto(req));
                    confirmedCount++;
                } else {
                    req.reject();
                    rejected.add(requestMapper.toDto(req));
                }
            }

            if (currentConfirmed + confirmedCount >= event.participantLimit()) {
                List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, Status.PENDING);
                for (Request pending : pendingRequests) {
                    pending.reject();
                    rejected.add(requestMapper.toDto(pending));
                }
                requestRepository.saveAll(pendingRequests);
            }
        } else { // REJECTED
            for (Request req : requestsForUpdate) {
                req.reject();
                rejected.add(requestMapper.toDto(req));
            }
        }

        requestRepository.saveAll(requestsForUpdate);
        log.info("Обновлено: подтверждено {}, отклонено {}", confirmed.size(), rejected.size());

        return requestMapper.toUpdateResult(confirmed, rejected);
    }

    // ========== Приватные вспомогательные методы ==========

    private void checkUserExists(long userId) {
        try {
            userClient.getUserById(userId);
        } catch (Exception e) {
            throw new NotFoundResource("Пользователь с id " + userId + " не найден");
        }
    }

    private Request getRequestOrThrow(long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundResource("Запрос с id " + requestId + " не найден"));
    }

    private void validateRequestsExist(List<Long> requestedIds, List<Request> found) {
        Set<Long> foundIds = found.stream().map(Request::getId).collect(Collectors.toSet());
        List<Long> missing = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new NotFoundResource("Запросы с ids " + missing + " не найдены");
        }
    }

    private void validateRequestsBelongToEvent(List<Request> requests, Long eventId) {
        for (Request req : requests) {
            if (!req.getEventId().equals(eventId)) {
                throw new ConflictResource("Запрос " + req.getId() + " не относится к событию " + eventId);
            }
        }
    }

    private void validateRequestsArePending(List<Request> requests) {
        for (Request req : requests) {
            if (req.getStatus() != Status.PENDING) {
                throw new ConflictResource("Запрос " + req.getId() + " не в статусе PENDING");
            }
        }
    }

    /**
     * Проверяет возможность перехода из текущего статуса в новый.
     *
     * @param currentStatus текущий статус
     * @param newStatus     новый статус
     * @throws ConflictResource если переход невозможен
     */
    private void validateStatusTransition(Status currentStatus, Status newStatus) {
        // Если статус не меняется - разрешаем
        if (currentStatus == newStatus) {
            return;
        }

        switch (currentStatus) {
            case PENDING:
                // Из PENDING можно перейти в любой статус
                break;
            case CONFIRMED:
                // Из CONFIRMED нельзя перейти в REJECTED или другие статусы
                throw new ConflictResource(
                        String.format("Невозможно изменить статус с %s на %s: подтверждённый запрос нельзя отклонить",
                                currentStatus, newStatus));
            case REJECTED:
                // Из REJECTED нельзя изменить статус
                throw new ConflictResource(
                        String.format("Невозможно изменить статус с %s на %s: отклонённый запрос нельзя изменить",
                                currentStatus, newStatus));
            case CANCELED:
                // Из CANCELED нельзя изменить статус
                throw new ConflictResource(
                        String.format("Невозможно изменить статус с %s на %s: отменённый запрос нельзя изменить",
                                currentStatus, newStatus));
            default:
                throw new ConflictResource(
                        String.format("Невозможно изменить статус с %s на %s", currentStatus, newStatus));
        }
    }
}