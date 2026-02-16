package ru.practicum.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.feignclients.client.EventClient;
import ru.practicum.feignclients.client.UserClient;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.service.RequestService;
import ru.practicum.request.util.Status;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        log.info("Getting requests for user {}", userId);
        checkUserExists(userId);

        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Creating request for user {} to event {}", userId, eventId);

        checkUserExists(userId);
        var event = eventClient.getEventById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw ConflictResource.ofValue(
                    "Инициатор события не может подать заявку на участие в своём событии",
                    userId + ":" + eventId
            );
        }

        if (!event.getState().name().equals("PUBLISHED")) {
            throw ConflictResource.ofValue(
                    "Нельзя участвовать в неопубликованном событии",
                    event.getState().name()
            );
        }

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw ConflictResource.ofValue(
                    "Заявка на участие в этом событии уже существует",
                    userId + ":" + eventId
            );
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw ConflictResource.ofValue(
                    "Достигнут лимит участников для этого события",
                    "limit: " + event.getParticipantLimit()
            );
        }

        Status status = Status.PENDING;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = Status.CONFIRMED;
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .eventId(eventId)
                .requesterId(userId)
                .status(status)
                .build();

        Request savedRequest = requestRepository.save(request);
        log.info("Created request with id: {}", savedRequest.getId());

        return RequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request {} for user {}", requestId, userId);

        checkUserExists(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundResource("Request", requestId));

        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundResource("Request", requestId);
        }

        request.setStatus(Status.CANCELED);
        Request updatedRequest = requestRepository.save(request);
        log.info("Cancelled request with id: {}", updatedRequest.getId());

        return RequestMapper.toDto(updatedRequest);
    }

    // ========== Методы для межсервисного взаимодействия ==========

    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(long eventId) {
        log.info("Getting requests for event {}", eventId);
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByIds(List<Long> ids) {
        log.info("Getting requests by ids: {}", ids);
        return requestRepository.findAllById(ids).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long countConfirmedRequestsByEventId(long eventId) {
        log.info("Counting confirmed requests for event {}", eventId);
        return requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
    }

    @Override
    public Map<Long, Long> countConfirmedRequestsByEventIds(List<Long> eventIds) {
        log.info("Counting confirmed requests for events: {}", eventIds);

        List<Object[]> results = requestRepository.countByEventIdInAndStatus(eventIds, Status.CONFIRMED);
        Map<Long, Long> counts = new HashMap<>();

        for (Object[] result : results) {
            Long eventId = (Long) result[0];
            Long count = (Long) result[1];
            counts.put(eventId, count);
        }

        // Заполняем нулями события, по которым нет записей
        for (Long eventId : eventIds) {
            counts.putIfAbsent(eventId, 0L);
        }

        return counts;
    }

    @Override
    @Transactional
    public ParticipationRequestDto updateRequestStatus(long requestId, Status status) {
        log.info("Updating request {} status to {}", requestId, status);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundResource("Request", requestId));

        request.setStatus(status);
        Request updatedRequest = requestRepository.save(request);
        log.info("Updated request {} status to {}", updatedRequest.getId(), status);

        return RequestMapper.toDto(updatedRequest);
    }

    /**
     * Проверяет существование пользователя.
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundResource если пользователь не найден
     */
    private void checkUserExists(long userId) {
        try {
            userClient.getUserById(userId);
        } catch (Exception e) {
            throw new NotFoundResource("User", userId);
        }
    }
}