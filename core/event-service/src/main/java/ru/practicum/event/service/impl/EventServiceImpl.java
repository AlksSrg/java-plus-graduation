package ru.practicum.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.event.util.EventGetAdminParam;
import ru.practicum.event.util.EventGetPublicParam;
import ru.practicum.event.util.EventSort;
import ru.practicum.event.util.State;
import ru.practicum.ewm.client.RecommendationsClient;
import ru.practicum.ewm.client.UserActionClient;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.feignclients.client.CategoryClient;
import ru.practicum.feignclients.client.RequestClient;
import ru.practicum.feignclients.client.UserClient;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.util.Status;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с событиями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final RequestClient requestClient;
    private final RecommendationsClient recommendationsClient;
    private final UserActionClient userActionClient;
    private final EventMapper eventMapper;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final String EVENT_NOT_FOUND_MESSAGE = "Событие с id = %d не найдено";
    private static final int MIN_HOURS_BEFORE_EVENT = 2;

    // ========================== Публичные методы (админ) ==========================

    @Override
    public List<EventFullDto> getEventsByAdmin(EventGetAdminParam param) {
        log.info("Поиск событий по параметрам администратора: {}", param);

        Specification<Event> spec = buildAdminSpecification(param);
        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        return enrichEventFullDtos(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Обновление события с id = {} администратором: {}", eventId, updateRequest);

        Event event = findEventById(eventId);

        // Обновление полей
        updateEventFields(event, updateRequest);

        // Обработка статуса
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    publishEvent(event);
                    break;
                case REJECT_EVENT:
                    rejectEvent(event);
                    break;
                default:
                    throw new BadRequestException("Некорректное действие: " + updateRequest.getStateAction());
            }
        }

        Event saved = eventRepository.save(event);
        log.info("Событие с id = {} успешно обновлено администратором", eventId);
        return enrichEventFullDto(saved);
    }

    // ========================== Публичные методы (публичный доступ) ==========================

    @Override
    public List<EventShortDto> getEventsByPublic(EventGetPublicParam param) {
        log.info("Поиск событий по публичным параметрам: {}", param);

        validateRange(param.getRangeStart(), param.getRangeEnd());

        Specification<Event> spec = buildPublicSpecification(param);
        Sort sort = buildSort(param.getSort());
        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize(), sort);

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        return enrichEventShortDtos(events);
    }

    @Override
    public EventFullDto getEventByPublic(Long id) {
        log.info("Получение события с id = {} для публичного доступа", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundResource(String.format(EVENT_NOT_FOUND_MESSAGE, id)));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundResource(String.format(EVENT_NOT_FOUND_MESSAGE, id));
        }

        return enrichEventFullDto(event);
    }

    // ========================== Методы пользователя ==========================

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя с id = {}", userId);
        userClient.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();
        return enrichEventShortDtos(events);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание нового события пользователем с id = {}: {}", userId, newEventDto);

        validateEventDate(newEventDto.getEventDate());

        UserDto user = userClient.getUserById(userId);
        CategoryDto category = categoryClient.getCategoryById(newEventDto.getCategory());

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiatorId(user.getId());
        event.setCategoryId(category.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);

        Event saved = eventRepository.save(event);
        log.info("Событие с id = {} успешно создано", saved.getId());

        return enrichEventFullDto(saved);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        log.info("Получение события с id = {} пользователя с id = {}", eventId, userId);

        userClient.getUserById(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Событие с id = %d у пользователя с id = %d не найдено", eventId, userId)));

        return enrichEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Обновление события с id = {} пользователем с id = {}: {}", eventId, userId, updateRequest);

        userClient.getUserById(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Событие с id = %d у пользователя с id = %d не найдено", eventId, userId)));

        if (event.getState() == State.PUBLISHED) {
            throw new ConflictResource("Нельзя изменить опубликованное событие");
        }

        updateEventFields(event, updateRequest);

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.sendToReview();
                    break;
                case CANCEL_REVIEW:
                    event.cancel();
                    break;
            }
        }

        Event saved = eventRepository.save(event);
        log.info("Событие с id = {} успешно обновлено пользователем", eventId);
        return enrichEventFullDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        log.info("Получение запросов для события {} пользователя {}", eventId, userId);
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Событие с id = %d у пользователя с id = %d не найдено", eventId, userId)));
        return requestClient.getRequestsByEventId(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.info("Обновление статусов запросов для события {} пользователя {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Событие с id = %d у пользователя с id = %d не найдено", eventId, userId)));

        // Проверка лимитов перед подтверждением
        if (request.getStatus() == Status.CONFIRMED) {
            long confirmedNow = requestClient.countConfirmedRequestsByEventId(eventId);
            if (event.getParticipantLimit() > 0 && confirmedNow >= event.getParticipantLimit()) {
                throw new ConflictResource("Достигнут лимит участников");
            }
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        for (Long reqId : request.getRequestIds()) {
            ParticipationRequestDto updated = requestClient.updateRequestStatus(reqId, request.getStatus());
            if (request.getStatus() == Status.CONFIRMED) {
                result.getConfirmedRequests().add(updated);
            } else {
                result.getRejectedRequests().add(updated);
            }
        }

        // Обновляем confirmedRequests в событии
        if (request.getStatus() == Status.CONFIRMED) {
            long totalConfirmed = requestClient.countConfirmedRequestsByEventId(eventId);
            event.setConfirmedRequests(totalConfirmed);
            eventRepository.save(event);
        }

        return result;
    }

    // ========================== Внутренние методы (для других микросервисов) ==========================

    @Override
    public EventFullDto getEventByIdInternal(Long eventId) {
        log.info("Internal call: получение события с id = {}", eventId);
        Event event = findEventById(eventId);
        return enrichEventFullDto(event);
    }

    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundResource(String.format(EVENT_NOT_FOUND_MESSAGE, eventId)));
    }

    @Override
    public Boolean existsEventById(Long eventId) {
        return eventRepository.existsById(eventId);
    }

    @Override
    public Boolean existsByCategoryId(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public List<EventShortDto> getEventsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Event> events = eventRepository.findByIdIn(ids);
        return enrichEventShortDtos(events);
    }

    @Override
    public EventShortDto getEventShortById(Long eventId) {
        Event event = findEventById(eventId);
        return enrichEventShortDto(event);
    }

    @Override
    public boolean hasUserParticipated(Long userId, Long eventId) {
        try {
            List<ParticipationRequestDto> requests = requestClient.getRequestsByEventId(eventId);
            return requests.stream()
                    .anyMatch(r -> r.getRequester().equals(userId) && r.getStatus() == Status.CONFIRMED);
        } catch (Exception e) {
            log.error("Ошибка при проверке участия пользователя {} в событии {}", userId, eventId, e);
            return false;
        }
    }

    // ========================== Лайки и рекомендации ==========================

    @Override
    @Transactional
    public void addLike(Long userId, Long eventId) {
        log.info("Добавление лайка: userId={}, eventId={}", userId, eventId);

        userClient.getUserById(userId);
        findEventById(eventId); // проверка существования события

        if (!hasUserParticipated(userId, eventId)) {
            throw new ConflictResource("Пользователь может ставить лайк только событиям, в которых участвовал");
        }

        executorService.submit(() -> {
            try {
                userActionClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE, Instant.now());
                log.debug("Лайк отправлен: userId={}, eventId={}", userId, eventId);
            } catch (Exception e) {
                log.error("Ошибка при отправке лайка", e);
            }
        });
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, int maxResults) {
        log.info("Получение рекомендаций для пользователя userId={}, maxResults={}", userId, maxResults);

        List<Long> recommendedIds = recommendationsClient.getRecommendationsForUser(userId, maxResults)
                .map(RecommendedEventProto::getEventId)
                .toList();

        if (recommendedIds.isEmpty()) {
            return List.of();
        }

        List<Event> events = eventRepository.findByIdIn(recommendedIds);
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(recommendedIds);
        Map<Long, Double> ratingMap = getRatingMap(recommendedIds);

        return events.stream()
                .map(event -> {
                    CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
                    UserShortDto user = getUserShort(event.getInitiatorId());
                    Long confirmed = confirmedMap.getOrDefault(event.getId(), 0L);
                    Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
                    return eventMapper.toShortDto(event, category, user, confirmed, rating);
                })
                .toList();
    }

    // ========================== Приватные вспомогательные методы ==========================

    private List<EventFullDto> enrichEventFullDtos(List<Event> events) {
        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Double> ratingMap = getRatingMap(eventIds);

        return events.stream()
                .map(event -> buildEventFullDto(event, confirmedMap, ratingMap))
                .toList();
    }

    private EventFullDto enrichEventFullDto(Event event) {
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(List.of(event.getId()));
        Map<Long, Double> ratingMap = getRatingMap(List.of(event.getId()));
        return buildEventFullDto(event, confirmedMap, ratingMap);
    }

    private EventFullDto buildEventFullDto(Event event, Map<Long, Long> confirmedMap, Map<Long, Double> ratingMap) {
        CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
        UserShortDto user = getUserShort(event.getInitiatorId());
        Long confirmed = confirmedMap.getOrDefault(event.getId(), 0L);
        Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
        return eventMapper.toFullDto(event, category, user, confirmed, rating);
    }

    private List<EventShortDto> enrichEventShortDtos(List<Event> events) {
        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Double> ratingMap = getRatingMap(eventIds);

        return events.stream()
                .map(event -> {
                    CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
                    UserShortDto user = getUserShort(event.getInitiatorId());
                    Long confirmed = confirmedMap.getOrDefault(event.getId(), 0L);
                    Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
                    return eventMapper.toShortDto(event, category, user, confirmed, rating);
                })
                .toList();
    }

    private EventShortDto enrichEventShortDto(Event event) {
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(List.of(event.getId()));
        Map<Long, Double> ratingMap = getRatingMap(List.of(event.getId()));
        CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
        UserShortDto user = getUserShort(event.getInitiatorId());
        Long confirmed = confirmedMap.getOrDefault(event.getId(), 0L);
        Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
        return eventMapper.toShortDto(event, category, user, confirmed, rating);
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();
        return requestClient.countConfirmedRequestsByEventIds(eventIds);
    }

    private Map<Long, Double> getRatingMap(List<Long> eventIds) {
        return recommendationsClient.getInteractionsCount(eventIds)
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }

    private UserShortDto getUserShort(Long userId) {
        try {
            return userClient.getUserShortById(userId);
        } catch (Exception e) {
            log.error("Не удалось получить информацию о пользователе {}", userId, e);
            throw new NotFoundResource("Пользователь с id " + userId + " не найден");
        }
    }

    private Specification<Event> buildAdminSpecification(EventGetAdminParam param) {
        Specification<Event> spec = Specification.where(null);

        if (param.getUsers() != null && !param.getUsers().isEmpty()) {
            spec = spec.and((root, q, cb) -> root.get("initiatorId").in(param.getUsers()));
        }
        if (param.getStates() != null && !param.getStates().isEmpty()) {
            spec = spec.and((root, q, cb) -> root.get("state").as(String.class).in(param.getStates()));
        }
        if (param.getCategories() != null && !param.getCategories().isEmpty()) {
            spec = spec.and((root, q, cb) -> root.get("categoryId").in(param.getCategories()));
        }
        if (param.getRangeStart() != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), param.getRangeStart()));
        }
        if (param.getRangeEnd() != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), param.getRangeEnd()));
        }
        return spec;
    }

    private Specification<Event> buildPublicSpecification(EventGetPublicParam param) {
        Specification<Event> spec = (root, q, cb) -> cb.equal(root.get("state"), State.PUBLISHED);

        if (param.getText() != null && !param.getText().isBlank()) {
            String pattern = "%" + param.getText().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            ));
        }
        if (param.getCategories() != null && !param.getCategories().isEmpty()) {
            spec = spec.and((root, q, cb) -> root.get("categoryId").in(param.getCategories()));
        }
        if (param.getPaid() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("paid"), param.getPaid()));
        }
        LocalDateTime start = param.getRangeStart() != null ? param.getRangeStart() : LocalDateTime.now();
        spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), start));
        if (param.getRangeEnd() != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), param.getRangeEnd()));
        }
        if (param.getOnlyAvailable() != null && param.getOnlyAvailable()) {
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.equal(root.get("participantLimit"), 0),
                    cb.lessThan(root.get("confirmedRequests"), root.get("participantLimit"))
            ));
        }
        return spec;
    }

    private Sort buildSort(String sortParam) {
        if (sortParam == null) return Sort.unsorted();
        if (sortParam.equalsIgnoreCase(EventSort.EVENT_DATE.toString())) {
            return Sort.by(Sort.Direction.ASC, "eventDate");
        }
        // Другие варианты сортировки можно добавить здесь
        return Sort.unsorted();
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("Дата окончания не может быть раньше даты начала");
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new BadRequestException(
                    String.format("Дата события должна быть не раньше чем через %d часа от текущего времени",
                            MIN_HOURS_BEFORE_EVENT));
        }
    }

    private void publishEvent(Event event) {
        if (event.getState() != State.PENDING) {
            throw new ConflictResource("Событие можно опубликовать только в статусе PENDING");
        }
        event.publish();
    }

    private void rejectEvent(Event event) {
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictResource("Нельзя отклонить опубликованное событие");
        }
        event.cancel();
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest request) {
        if (request.hasAnnotation()) event.setAnnotation(request.getAnnotation());
        if (request.hasCategory()) {
            CategoryDto cat = categoryClient.getCategoryById(request.getCategory());
            event.setCategoryId(cat.getId());
        }
        if (request.hasDescription()) event.setDescription(request.getDescription());
        if (request.hasEventDate()) {
            validateEventDate(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }
        if (request.hasLocation()) event.setLocation(request.getLocation());
        if (request.hasPaid()) event.setPaid(request.getPaid());
        if (request.hasParticipantLimit()) event.setParticipantLimit(request.getParticipantLimit());
        if (request.hasRequestModeration()) event.setRequestModeration(request.getRequestModeration());
        if (request.hasTitle()) event.setTitle(request.getTitle());
    }

    private void updateEventFields(Event event, UpdateEventUserRequest request) {
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategory() != null) {
            CategoryDto cat = categoryClient.getCategoryById(request.getCategory());
            event.setCategoryId(cat.getId());
        }
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) {
            validateEventDate(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
    }
}