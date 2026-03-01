package ru.practicum.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import ru.practicum.event.util.StateActionUser;
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
import java.util.List;
import java.util.Map;
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
    private final EventMapper eventMapper;

    private static final String EVENT_NOT_FOUND_MESSAGE = "Событие с id = %d не найдено";
    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MAX_ANNOTATION_LENGTH = 2000;
    private static final int MAX_DESCRIPTION_LENGTH = 7000;
    private static final int MIN_HOURS_BEFORE_EVENT = 2;

    @Override
    public List<EventFullDto> getEventsByAdmin(EventGetAdminParam param) {
        log.info("Поиск событий по параметрам администратора: {}", param);

        Specification<Event> specification = Specification.where(null);

        if (param.getUsers() != null && !param.getUsers().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiatorId").in(param.getUsers()));
        }

        if (param.getStates() != null && !param.getStates().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("state").in(param.getStates()));
        }

        if (param.getCategories() != null && !param.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("categoryId").in(param.getCategories()));
        }

        if (param.getRangeStart() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), param.getRangeStart()));
        }

        if (param.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), param.getRangeEnd()));
        }

        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());

        Page<Event> eventPage = eventRepository.findAll(specification, pageable);
        List<Event> events = eventPage.getContent();
        log.info("Найдено {} событий", events.size());

        // Получаем количество подтвержденных запросов для всех событий
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedCounts = requestClient.countConfirmedRequestsByEventIds(eventIds);

        return events.stream()
                .map(event -> {
                    CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
                    UserShortDto user = getUserShortDto(event.getInitiatorId());
                    // Обновляем confirmedRequests из ответа request-service
                    event.setConfirmedRequests(confirmedCounts.getOrDefault(event.getId(), 0L));
                    return eventMapper.toFullDto(event, category, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Обновление события с id = {} администратором: {}", eventId, updateRequest);

        Event event = findEventById(eventId);

        if (updateRequest.getAnnotation() != null) {
            validateAnnotation(updateRequest.getAnnotation());
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            CategoryDto category = categoryClient.getCategoryById(updateRequest.getCategory());
            event.setCategoryId(category.getId());
        }

        if (updateRequest.getDescription() != null) {
            validateDescription(updateRequest.getDescription());
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            validateEventDate(updateRequest.getEventDate());
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null) {
            validateTitle(updateRequest.getTitle());
            event.setTitle(updateRequest.getTitle());
        }

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

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с id = {} успешно обновлено", eventId);

        CategoryDto category = categoryClient.getCategoryById(updatedEvent.getCategoryId());
        UserShortDto user = getUserShortDto(updatedEvent.getInitiatorId());
        // Получаем актуальное количество подтвержденных запросов
        Long confirmedCount = requestClient.countConfirmedRequestsByEventId(eventId);
        updatedEvent.setConfirmedRequests(confirmedCount);

        return eventMapper.toFullDto(updatedEvent, category, user);
    }

    @Override
    public List<EventShortDto> getEventsByPublic(EventGetPublicParam param) {
        log.info("Поиск событий по публичным параметрам: {}", param);

        if (param.getRangeStart() != null && param.getRangeEnd() != null) {
            if (param.getRangeEnd().isBefore(param.getRangeStart())) {
                throw new BadRequestException("Дата окончания не может быть раньше даты начала");
            }
        }

        Specification<Event> specification = Specification.where((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), State.PUBLISHED));

        if (param.getText() != null && !param.getText().isBlank()) {
            String searchText = "%" + param.getText().toLowerCase() + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), searchText),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchText)
                    ));
        }

        if (param.getCategories() != null && !param.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("categoryId").in(param.getCategories()));
        }

        if (param.getPaid() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("paid"), param.getPaid()));
        }

        if (param.getRangeStart() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), param.getRangeStart()));
        } else {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
        }

        if (param.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), param.getRangeEnd()));
        }

        if (param.getOnlyAvailable() != null && param.getOnlyAvailable()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("participantLimit"), 0),
                            criteriaBuilder.lessThan(
                                    root.get("confirmedRequests"),
                                    root.get("participantLimit")
                            )
                    ));
        }

        Sort sort = Sort.unsorted();
        if (param.getSort() != null) {
            // В EventSort нет RATING, пока оставляем сортировку только по EVENT_DATE
            if (param.getSort().equals(EventSort.EVENT_DATE.toString())) {
                sort = Sort.by(Sort.Direction.ASC, "eventDate");
            } else {
                log.warn("Неизвестный тип сортировки: {}", param.getSort());
            }
        }

        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize(), sort);
        Page<Event> eventPage = eventRepository.findAll(specification, pageable);
        List<Event> events = eventPage.getContent();
        log.info("Найдено {} опубликованных событий", events.size());

        // Получаем количество подтвержденных запросов для всех событий
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedCounts = requestClient.countConfirmedRequestsByEventIds(eventIds);

        return events.stream()
                .map(event -> {
                    CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
                    UserShortDto user = getUserShortDto(event.getInitiatorId());
                    // Обновляем confirmedRequests из ответа request-service
                    event.setConfirmedRequests(confirmedCounts.getOrDefault(event.getId(), 0L));
                    return eventMapper.toShortDto(event, category, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByPublic(Long id) {
        log.info("Получение события с id = {} для публичного доступа", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundResource(String.format(EVENT_NOT_FOUND_MESSAGE, id)));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundResource(String.format(EVENT_NOT_FOUND_MESSAGE, id));
        }

        log.info("Событие с id = {} найдено", id);

        // Получаем актуальное количество подтвержденных запросов
        Long confirmedCount = requestClient.countConfirmedRequestsByEventId(id);
        event.setConfirmedRequests(confirmedCount);

        CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
        UserShortDto user = getUserShortDto(event.getInitiatorId());
        return eventMapper.toFullDto(event, category, user);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя с id = {}", userId);

        userClient.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> eventPage = eventRepository.findByInitiatorId(userId, pageable);
        List<Event> events = eventPage.getContent();

        log.info("Найдено {} событий пользователя с id = {}", events.size(), userId);

        // Получаем количество подтвержденных запросов для всех событий
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedCounts = requestClient.countConfirmedRequestsByEventIds(eventIds);

        return events.stream()
                .map(event -> {
                    CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
                    UserShortDto user = getUserShortDto(event.getInitiatorId());
                    // Обновляем confirmedRequests из ответа request-service
                    event.setConfirmedRequests(confirmedCounts.getOrDefault(event.getId(), 0L));
                    return eventMapper.toShortDto(event, category, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание нового события пользователем с id = {}: {}", userId, newEventDto);

        validateEventDate(newEventDto.getEventDate());
        validateTitle(newEventDto.getTitle());
        validateAnnotation(newEventDto.getAnnotation());
        validateDescription(newEventDto.getDescription());

        UserDto user = userClient.getUserById(userId);
        CategoryDto category = categoryClient.getCategoryById(newEventDto.getCategory());

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiatorId(user.getId());
        event.setCategoryId(category.getId());

        Event savedEvent = eventRepository.save(event);
        log.info("Событие с id = {} успешно создано", savedEvent.getId());

        UserShortDto userShort = getUserShortDto(user.getId());
        return eventMapper.toFullDto(savedEvent, category, userShort);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        log.info("Получение события с id = {} пользователя с id = {}", eventId, userId);

        userClient.getUserById(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Событие с id = %d у пользователя с id = %d не найдено", eventId, userId)));

        // Получаем актуальное количество подтвержденных запросов
        Long confirmedCount = requestClient.countConfirmedRequestsByEventId(eventId);
        event.setConfirmedRequests(confirmedCount);

        CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
        UserShortDto user = getUserShortDto(event.getInitiatorId());
        return eventMapper.toFullDto(event, category, user);
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

        if (updateRequest.getAnnotation() != null) {
            validateAnnotation(updateRequest.getAnnotation());
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            CategoryDto category = categoryClient.getCategoryById(updateRequest.getCategory());
            event.setCategoryId(category.getId());
        }

        if (updateRequest.getDescription() != null) {
            validateDescription(updateRequest.getDescription());
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            validateEventDate(updateRequest.getEventDate());
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null) {
            validateTitle(updateRequest.getTitle());
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == StateActionUser.CANCEL_REVIEW) {
                event.setState(State.CANCELED);
            } else if (updateRequest.getStateAction() == StateActionUser.SEND_TO_REVIEW) {
                event.setState(State.PENDING);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с id = {} успешно обновлено пользователем", eventId);

        // Получаем актуальное количество подтвержденных запросов
        Long confirmedCount = requestClient.countConfirmedRequestsByEventId(eventId);
        updatedEvent.setConfirmedRequests(confirmedCount);

        CategoryDto category = categoryClient.getCategoryById(updatedEvent.getCategoryId());
        UserShortDto user = getUserShortDto(updatedEvent.getInitiatorId());
        return eventMapper.toFullDto(updatedEvent, category, user);
    }

    @Override
    public EventFullDto getEventByIdInternal(Long eventId) {
        log.info("Internal call: получение события с id = {}", eventId);
        Event event = findEventById(eventId);

        // Получаем актуальное количество подтвержденных запросов
        Long confirmedCount = requestClient.countConfirmedRequestsByEventId(eventId);
        event.setConfirmedRequests(confirmedCount);

        CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
        UserShortDto user = getUserShortDto(event.getInitiatorId());
        return eventMapper.toFullDto(event, category, user);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        log.info("Получение запросов для события {} пользователя {}", eventId, userId);
        // Проверяем, что событие принадлежит пользователю
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
        // Проверяем, что событие принадлежит пользователю
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Событие с id = %d у пользователя с id = %d не найдено", eventId, userId)));

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        // Обрабатываем каждый запрос индивидуально
        for (Long requestId : request.getRequestIds()) {
            ParticipationRequestDto updatedRequest = requestClient.updateRequestStatus(requestId, request.getStatus());

            if (request.getStatus() == Status.CONFIRMED) {
                // Обновляем количество подтвержденных запросов
                Long confirmedCount = requestClient.countConfirmedRequestsByEventId(eventId);
                event.setConfirmedRequests(confirmedCount);
                eventRepository.save(event);

                result.getConfirmedRequests().add(updatedRequest);
            } else {
                result.getRejectedRequests().add(updatedRequest);
            }
        }

        return result;
    }

    @Override
    public List<EventShortDto> getEventsByIds(List<Long> ids) {
        log.info("Получение событий по идентификаторам: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Event> events = eventRepository.findAllById(ids);
        log.info("Найдено {} событий", events.size());

        // Получаем количество подтвержденных запросов для всех событий
        Map<Long, Long> confirmedCounts = requestClient.countConfirmedRequestsByEventIds(ids);

        return events.stream()
                .map(event -> {
                    CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
                    UserShortDto user = getUserShortDto(event.getInitiatorId());
                    // Обновляем confirmedRequests из ответа request-service
                    event.setConfirmedRequests(confirmedCounts.getOrDefault(event.getId(), 0L));
                    return eventMapper.toShortDto(event, category, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventShortDto getEventShortById(Long eventId) {
        log.info("Получение краткой информации о событии с id = {}", eventId);

        Event event = findEventById(eventId);

        // Получаем актуальное количество подтвержденных запросов
        Long confirmedCount = requestClient.countConfirmedRequestsByEventId(eventId);
        event.setConfirmedRequests(confirmedCount);

        CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
        UserShortDto user = getUserShortDto(event.getInitiatorId());
        return eventMapper.toShortDto(event, category, user);
    }

    @Override
    public Boolean existsEventById(Long eventId) {
        log.info("Проверка существования события с id = {}", eventId);
        return eventRepository.existsById(eventId);
    }

    @Override
    public Boolean existsByCategoryId(Long categoryId) {
        log.info("Проверка существования событий с категорией id = {}", categoryId);
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public Event findEventById(Long eventId) {
        log.info("Поиск события по id = {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundResource(String.format(EVENT_NOT_FOUND_MESSAGE, eventId)));
    }

    @Override
    public boolean hasUserParticipated(Long userId, Long eventId) {
        log.info("Проверка участия пользователя {} в событии {}", userId, eventId);

        try {
            List<ParticipationRequestDto> requests = requestClient.getRequestsByEventId(eventId);
            boolean participated = requests.stream()
                    .anyMatch(request -> request.getRequester().equals(userId) &&
                            request.getStatus() == Status.CONFIRMED);

            log.info("Пользователь {} {} в событии {}",
                    userId, participated ? "участвовал" : "не участвовал", eventId);
            return participated;
        } catch (Exception e) {
            log.error("Ошибка при проверке участия пользователя {} в событии {}", userId, eventId, e);
            return false;
        }
    }

    /**
     * Получает краткую информацию о пользователе.
     *
     * @param userId идентификатор пользователя
     * @return краткое DTO пользователя
     */
    private UserShortDto getUserShortDto(Long userId) {
        try {
            return userClient.getUserShortById(userId);
        } catch (Exception e) {
            log.warn("Не удалось получить краткую информацию о пользователе {}", userId);
            return null;
        }
    }

    /**
     * Публикует событие.
     *
     * @param event событие для публикации
     */
    private void publishEvent(Event event) {
        if (event.getState() != State.PENDING) {
            throw new ConflictResource("Событие можно опубликовать только в статусе PENDING");
        }
        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    /**
     * Отклоняет событие.
     *
     * @param event событие для отклонения
     */
    private void rejectEvent(Event event) {
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictResource("Нельзя отклонить опубликованное событие");
        }
        event.setState(State.CANCELED);
    }

    /**
     * Проверяет дату события.
     *
     * @param eventDate дата события
     */
    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new BadRequestException(
                    String.format("Дата события должна быть не раньше чем через %d часа от текущего времени",
                            MIN_HOURS_BEFORE_EVENT));
        }
    }

    /**
     * Проверяет заголовок события.
     *
     * @param title заголовок события
     */
    private void validateTitle(String title) {
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new BadRequestException(
                    String.format("Максимальная длина заголовка - %d символов", MAX_TITLE_LENGTH));
        }
    }

    /**
     * Проверяет аннотацию события.
     *
     * @param annotation аннотация события
     */
    private void validateAnnotation(String annotation) {
        if (annotation.length() > MAX_ANNOTATION_LENGTH) {
            throw new BadRequestException(
                    String.format("Максимальная длина аннотации - %d символов", MAX_ANNOTATION_LENGTH));
        }
    }

    /**
     * Проверяет описание события.
     *
     * @param description описание события
     */
    private void validateDescription(String description) {
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestException(
                    String.format("Максимальная длина описания - %d символов", MAX_DESCRIPTION_LENGTH));
        }
    }
}