package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.utill.State;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.utill.Status;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация сервиса для работы с заявками на участие в событиях.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        checkUserExists(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictResource("Инициатор события не может подать заявку на участие в своём событии");
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictResource("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictResource("Заявка на участие в этом событии уже существует");
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictResource("Достигнут лимит участников для этого события");
        }

        Status status = Status.PENDING;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = Status.CONFIRMED;
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(status)
                .build();

        Request savedRequest = requestRepository.save(request);
        return RequestMapper.mapToParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUserExists(userId);
        Request request = getRequestByIdAndRequesterId(requestId, userId);

        request.setStatus(Status.CANCELED);
        Request updatedRequest = requestRepository.save(request);

        return RequestMapper.mapToParticipationRequestDto(updatedRequest);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundResource("Пользователь с id=" + userId + " не найден"));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundResource("Событие с id=" + eventId + " не найдено"));
    }

    private Request getRequestByIdAndRequesterId(Long requestId, Long requesterId) {
        return requestRepository.findByIdAndRequesterId(requestId, requesterId)
                .orElseThrow(() -> new NotFoundResource("Заявка с id=" + requestId + " не найдена"));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundResource("Пользователь с id=" + userId + " не найден");
        }
    }
}