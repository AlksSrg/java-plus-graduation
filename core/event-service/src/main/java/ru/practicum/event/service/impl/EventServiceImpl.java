package ru.practicum.event.service.impl;

import dto.ViewStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.event.util.EventGetAdminParam;
import ru.practicum.event.util.EventGetPublicParam;
import ru.practicum.event.util.State;
import ru.practicum.event.util.StateActionAdmin;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.feignclients.client.CategoryClient;
import ru.practicum.feignclients.client.RequestClient;
import ru.practicum.feignclients.client.UserClient;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.util.Status;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.event.specification.EventSpecification.*;

/**
 * Реализация сервиса для работы с событиями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final String EVENT_URI_PATTERN = "/events/%d";
    private static final String APP_NAME = "event-service";

    private final EventRepository eventRepository;
    private final CategoryClient categoryClient;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final StatsClient statsClient;

    @Override
    public List<ParticipationRequestDto> getRequests(long userId, long eventId) {
        log.info("Getting requests for event {} by user {}", eventId, userId);

        // Проверяем существование события и права пользователя
        Event event = getEventByIdAndInitiatorId(eventId, userId);

        // Получаем запросы через RequestClient
        return requestClient.getRequestsByEventId(eventId);
    }

    @Override
    public EventFullDto get(long userId, long eventId) {
        log.info("Getting event {} for user {}", eventId, userId);

        Event event = getEventByIdAndInitiatorId(eventId, userId);
        return enrichEventWithDetails(event);
    }

    @Override
    public List<EventShortDto> getAll(long userId, int from, int size) {
        log.info("Getting all events for user {} with from={}, size={}", userId, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        return enrichEventsWithDetails(events);
    }

    @Override
    @Transactional
    public EventFullDto create(long userId, NewEventDto eventDto) {
        log.info("Creating new event for user {}: {}", userId, eventDto);

        // Проверяем дату события
        if (!eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата должна быть не ранее текущей + 2 часа");
        }

        // Проверяем существование категории через CategoryClient
        CategoryDto categoryDto = categoryClient.getCategoryById(eventDto.getCategory());

        // Проверяем существование пользователя через UserClient
        UserDto userDto = userClient.getUserById(userId);
        UserShortDto userShortDto = UserShortDto.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .build();

        // Создаем событие
        Event event = EventMapper.toEntity(eventDto, categoryDto.getId(), userDto.getId());
        Event savedEvent = eventRepository.save(event);
        log.info("Created event with id: {}", savedEvent.getId());

        return EventMapper.toFullDto(savedEvent, categoryDto, userShortDto, 0L, 0L);
    }

    @Override
    @Transactional
    public EventFullDto update(long userId, long eventId, UpdateEventUserRequest updateEvent) {
        log.info("Updating event {} for user {} with data: {}", eventId, userId, updateEvent);

        Event event = getEventByIdAndInitiatorId(eventId, userId);

        if (event.getState() == State.PUBLISHED) {
            throw new ConflictResource("Нельзя редактировать опубликованное событие");
        }

        if (updateEvent.getEventDate() != null &&
                updateEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        Long categoryId = null;
        if (updateEvent.getCategory() != null) {
            CategoryDto categoryDto = categoryClient.getCategoryById(updateEvent.getCategory());
            categoryId = categoryDto.getId();
        }

        EventMapper.updateFromUser(event, updateEvent, categoryId);

        if (updateEvent.getStateAction() != null) {
            switch (updateEvent.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(State.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(State.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Updated event with id: {}", updatedEvent.getId());

        return enrichEventWithDetails(updatedEvent);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(long userId, long eventId,
                                                              EventRequestStatusUpdateRequest eventRequestStatus) {
        log.info("Updating request status for event {} by user {}: {}", eventId, userId, eventRequestStatus);

        Event event = getEventByIdAndInitiatorId(eventId, userId);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictResource("Подтверждение заявок не требуется для этого события");
        }

        // Получаем текущие запросы через RequestClient
        List<ParticipationRequestDto> requests = requestClient.getRequestsByIds(eventRequestStatus.getRequestIds());

        // Проверяем, что все запросы принадлежат этому событию
        for (ParticipationRequestDto request : requests) {
            if (request.getEvent() != eventId) {
                throw new ConflictResource("Запрос " + request.getId() + " не относится к событию " + eventId);
            }
        }

        // Получаем количество подтвержденных запросов
        Long confirmedCount = requestClient.countConfirmedRequestsByEventId(eventId);

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        // Обновляем статусы через RequestClient
        for (Long requestId : eventRequestStatus.getRequestIds()) {
            if (eventRequestStatus.getStatus() == Status.CONFIRMED) {
                if (event.getParticipantLimit() == 0 || confirmedCount < event.getParticipantLimit()) {
                    ParticipationRequestDto updated = requestClient.updateRequestStatus(requestId, Status.CONFIRMED);
                    confirmed.add(updated);
                    confirmedCount++;
                } else {
                    ParticipationRequestDto updated = requestClient.updateRequestStatus(requestId, Status.REJECTED);
                    rejected.add(updated);
                }
            } else {
                ParticipationRequestDto updated = requestClient.updateRequestStatus(requestId, Status.REJECTED);
                rejected.add(updated);
            }
        }

        log.info("Updated request status: confirmed={}, rejected={}", confirmed.size(), rejected.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(EventGetAdminParam param) {
        log.info("Getting events by admin with params: {}", param);

        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        Specification<Event> specification = buildAdminSpecification(param);

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        return enrichEventsToFullDto(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest updateEvent) {
        log.info("Updating event {} by admin with data: {}", eventId, updateEvent);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundResource("Event", eventId));

        checkUpdateEventAdmin(event, updateEvent);

        Long categoryId = null;
        if (updateEvent.hasCategory()) {
            CategoryDto categoryDto = categoryClient.getCategoryById(updateEvent.getCategory());
            categoryId = categoryDto.getId();
        }

        EventMapper.updateFromAdmin(event, updateEvent, categoryId);

        if (updateEvent.hasStateAction()) {
            switch (updateEvent.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(State.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Updated event by admin with id: {}", updatedEvent.getId());

        return enrichEventWithDetails(updatedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByPublic(EventGetPublicParam param) {
        log.info("Getting events by public with params: {}", param);

        if (param.getRangeStart() != null && param.getRangeEnd() != null
                && param.getRangeEnd().isBefore(param.getRangeStart())) {
            throw new BadRequestException("Некорректный интервал дат");
        }

        Specification<Event> specification = buildPublicSpecification(param);
        Pageable pageable = buildPageable(param);

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        List<EventShortDto> result = enrichEventsWithDetails(events);

        // Сохраняем статистику просмотров (асинхронно в контроллере)

        return result;
    }

    @Override
    public EventFullDto getEventByPublic(long eventId) {
        log.info("Getting event {} by public", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundResource("Event", eventId));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundResource("Event", eventId);
        }

        return enrichEventWithDetails(event);
    }

    @Override
    public Event getEventById(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundResource("Event", eventId));
    }

    // Новые методы для Feign-клиентов

    @Override
    public EventShortDto getEventShortById(Long eventId) {
        log.info("Getting short event by id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundResource("Event", eventId));

        CategoryDto categoryDto = categoryClient.getCategoryById(event.getCategoryId());
        UserDto userDto = userClient.getUserById(event.getInitiatorId());

        UserShortDto userShortDto = UserShortDto.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .build();

        Long confirmedRequests = requestClient.countConfirmedRequestsByEventId(eventId);
        Long views = getViewsForEvent(eventId);

        return EventMapper.toShortDto(event, categoryDto, userShortDto, confirmedRequests, views);
    }

    @Override
    public List<EventShortDto> getEventsByIds(List<Long> ids) {
        log.info("Getting events by ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findByIdIn(ids);
        return enrichEventsWithDetails(events);
    }

    @Override
    public Boolean existsEventById(Long eventId) {
        log.info("Checking if event exists by id: {}", eventId);
        return eventRepository.existsById(eventId);
    }

    @Override
    public Boolean existsByCategoryId(Long categoryId) {
        log.info("Checking if events exist by category id: {}", categoryId);
        return eventRepository.existsByCategoryId(categoryId);
    }

    // Приватные вспомогательные методы

    private Event getEventByIdAndInitiatorId(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundResource("Event", eventId));
    }

    private Specification<Event> buildAdminSpecification(EventGetAdminParam param) {
        Specification<Event> specification = Specification.where(null);

        if (param.getUsers() != null && !param.getUsers().isEmpty()) {
            specification = specification.and(byUsers(param.getUsers()));
        }

        if (param.getStates() != null && !param.getStates().isEmpty()) {
            specification = specification.and(byStates(param.getStates()));
        }

        if (param.getCategories() != null && !param.getCategories().isEmpty()) {
            specification = specification.and(byCategories(param.getCategories()));
        }

        if (param.getRangeStart() != null) {
            specification = specification.and(byRangeStart(param.getRangeStart()));
        }

        if (param.getRangeEnd() != null) {
            specification = specification.and(byRangeEnd(param.getRangeEnd()));
        }

        return specification;
    }

    private Specification<Event> buildPublicSpecification(EventGetPublicParam param) {
        Specification<Event> specification = Specification.where(publishedOnly());

        if (param.getText() != null && !param.getText().isBlank()) {
            specification = specification.and(byText(param.getText()));
        }

        if (param.getCategories() != null && !param.getCategories().isEmpty()) {
            specification = specification.and(byCategories(param.getCategories()));
        }

        if (param.getPaid() != null) {
            specification = specification.and(byPaid(param.getPaid()));
        }

        LocalDateTime rangeStart = param.getRangeStart();
        LocalDateTime rangeEnd = param.getRangeEnd();

        if (rangeStart != null) {
            specification = specification.and(byRangeStart(rangeStart));
        }

        if (rangeEnd != null) {
            specification = specification.and(byRangeEnd(rangeEnd));
        }

        if (rangeStart == null && rangeEnd == null) {
            specification = specification.and(byRangeStart(LocalDateTime.now()));
        }

        if (param.getOnlyAvailable() != null && param.getOnlyAvailable()) {
            specification = specification.and(onlyAvailable());
        }

        return specification;
    }

    private Pageable buildPageable(EventGetPublicParam param) {
        Sort sort = Sort.unsorted();

        if (param.getSort() != null) {
            if (param.getSort().equals("EVENT_DATE")) {
                sort = Sort.by("eventDate");
            }
            // VIEWS сортировка будет применена после получения данных
        }

        return PageRequest.of(param.getFrom() / param.getSize(), param.getSize(), sort);
    }

    private EventFullDto enrichEventWithDetails(Event event) {
        // Получаем данные категории
        CategoryDto categoryDto = categoryClient.getCategoryById(event.getCategoryId());

        // Получаем данные пользователя
        UserDto userDto = userClient.getUserById(event.getInitiatorId());
        UserShortDto userShortDto = UserShortDto.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .build();

        // Получаем количество подтвержденных запросов
        Long confirmedRequests = requestClient.countConfirmedRequestsByEventId(event.getId());

        // Получаем количество просмотров
        Long views = getViewsForEvent(event.getId());

        return EventMapper.toFullDto(event, categoryDto, userShortDto, confirmedRequests, views);
    }

    private List<EventShortDto> enrichEventsWithDetails(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        // Получаем ID категорий и пользователей
        Set<Long> categoryIds = events.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());
        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        // Получаем данные категорий
        Map<Long, CategoryDto> categoryMap = categoryClient.getCategoriesByIds(new ArrayList<>(categoryIds))
                .stream()
                .collect(Collectors.toMap(CategoryDto::getId, Function.identity()));

        // Получаем данные пользователей
        Map<Long, UserDto> userMap = userClient.getUsersByIds(new ArrayList<>(userIds))
                .stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        // Получаем количество подтвержденных запросов для всех событий
        List<Long> eventIdList = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = requestClient.countConfirmedRequestsByEventIds(eventIdList);

        // Получаем просмотры для всех событий
        Map<Long, Long> viewsMap = getViewsForEvents(events);

        List<EventShortDto> result = new ArrayList<>();
        for (Event event : events) {
            CategoryDto categoryDto = categoryMap.get(event.getCategoryId());
            UserDto userDto = userMap.get(event.getInitiatorId());

            if (categoryDto == null || userDto == null) {
                log.warn("Skipping event {} due to missing category or user data", event.getId());
                continue;
            }

            UserShortDto userShortDto = UserShortDto.builder()
                    .id(userDto.getId())
                    .name(userDto.getName())
                    .build();

            Long confirmed = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
            Long views = viewsMap.getOrDefault(event.getId(), 0L);

            result.add(EventMapper.toShortDto(event, categoryDto, userShortDto, confirmed, views));
        }

        // Сортируем по просмотрам, если нужно
        if (!result.isEmpty() && events.stream().findFirst().map(Event::getState).orElse(null) == State.PUBLISHED) {
            // Сортировка по VIEWS будет применена в контроллере
        }

        return result;
    }

    private List<EventFullDto> enrichEventsToFullDto(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        // Получаем ID категорий и пользователей
        Set<Long> categoryIds = events.stream()
                .map(Event::getCategoryId)
                .collect(Collectors.toSet());
        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        // Получаем данные категорий
        Map<Long, CategoryDto> categoryMap = categoryClient.getCategoriesByIds(new ArrayList<>(categoryIds))
                .stream()
                .collect(Collectors.toMap(CategoryDto::getId, Function.identity()));

        // Получаем данные пользователей
        Map<Long, UserDto> userMap = userClient.getUsersByIds(new ArrayList<>(userIds))
                .stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        // Получаем количество подтвержденных запросов для всех событий
        List<Long> eventIdList = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = requestClient.countConfirmedRequestsByEventIds(eventIdList);

        // Получаем просмотры для всех событий
        Map<Long, Long> viewsMap = getViewsForEvents(events);

        List<EventFullDto> result = new ArrayList<>();
        for (Event event : events) {
            CategoryDto categoryDto = categoryMap.get(event.getCategoryId());
            UserDto userDto = userMap.get(event.getInitiatorId());

            if (categoryDto == null || userDto == null) {
                log.warn("Skipping event {} due to missing category or user data", event.getId());
                continue;
            }

            UserShortDto userShortDto = UserShortDto.builder()
                    .id(userDto.getId())
                    .name(userDto.getName())
                    .build();

            Long confirmed = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
            Long views = viewsMap.getOrDefault(event.getId(), 0L);

            result.add(EventMapper.toFullDto(event, categoryDto, userShortDto, confirmed, views));
        }

        return result;
    }

    private Long getViewsForEvent(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(5);
            LocalDateTime end = LocalDateTime.now();

            List<ViewStatsDto> stats = statsClient.getStats(
                    start, end,
                    List.of(String.format(EVENT_URI_PATTERN, eventId)),
                    true
            );

            return stats.isEmpty() ? 0L : stats.getFirst().getHits();
        } catch (Exception e) {
            log.warn("Failed to get views for event {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }

    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<String> uris = events.stream()
                    .map(event -> String.format(EVENT_URI_PATTERN, event.getId()))
                    .collect(Collectors.toList());

            LocalDateTime start = events.stream()
                    .map(Event::getCreatedOn)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now().minusYears(5));

            LocalDateTime end = LocalDateTime.now();

            List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> extractEventIdFromUri(stat.getUri()),
                            ViewStatsDto::getHits,
                            Long::sum
                    ));
        } catch (Exception e) {
            log.warn("Failed to get views for events: {}", e.getMessage());
            return events.stream()
                    .collect(Collectors.toMap(Event::getId, e1 -> 0L));
        }
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            log.warn("Failed to extract event id from uri: {}", uri);
            return 0L;
        }
    }

    private void checkUpdateEventAdmin(Event event, UpdateEventAdminRequest updateEvent) {
        LocalDateTime eventDate;

        if (updateEvent.hasStateAction()) {
            switch (updateEvent.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != State.PENDING) {
                        throw new ConflictResource("Событие можно публиковать только в статусе 'Ожидание'");
                    }
                    break;
                case REJECT_EVENT:
                    if (event.getState() == State.PUBLISHED) {
                        throw new ConflictResource("Событие можно отклонить, только если оно еще не опубликовано");
                    }
                    break;
            }
        }

        if (updateEvent.hasEventDate()) {
            eventDate = updateEvent.getEventDate();
        } else {
            eventDate = event.getEventDate();
        }

        if (updateEvent.hasStateAction() && updateEvent.getStateAction() == StateActionAdmin.PUBLISH_EVENT &&
                !eventDate.isAfter(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException(
                    "Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }
    }
}