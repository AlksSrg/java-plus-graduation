package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.CommentService;
import ru.practicum.comment.util.CommentGetParam;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.util.State;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.feignclients.client.EventClient;
import ru.practicum.feignclients.client.UserClient;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация сервиса комментариев.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public CommentDto getUserComment(Long userId, Long commentId) {
        log.info("Запрос комментария userId={}, commentId={}", userId, commentId);
        userClient.getUserById(userId);
        Comment comment = findCommentById(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("Комментарий принадлежит другому пользователю");
        }
        return enrichCommentDto(comment);
    }

    @Override
    public List<CommentDto> getUserComments(Long userId) {
        log.info("Запрос всех комментариев пользователя userId={}", userId);
        userClient.getUserById(userId);
        return commentRepository.findAllByAuthorId(userId).stream()
                .map(this::enrichCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        log.info("Создание комментария: userId={}, eventId={}", userId, eventId);

        UserDto author = userClient.getUserById(userId);
        EventFullDto event = eventClient.getEventById(eventId);

        if (event.state() != State.PUBLISHED) {
            throw new ConflictResource("Нельзя комментировать неопубликованное событие");
        }

        if (commentRepository.existsByAuthorIdAndEventId(userId, eventId)) {
            throw new ConflictResource("Пользователь уже оставил комментарий к этому событию");
        }

        Comment comment = commentMapper.toEntity(dto);
        comment.setAuthorId(userId);
        comment.setEventId(eventId);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        return enrichCommentDto(saved);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        log.info("Обновление комментария: userId={}, commentId={}", userId, commentId);

        userClient.getUserById(userId);
        Comment comment = findCommentById(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("Нельзя редактировать чужой комментарий");
        }

        commentMapper.updateEntityFromDto(dto, comment);
        Comment updated = commentRepository.save(comment);
        return enrichCommentDto(updated);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        log.info("Удаление комментария пользователем: userId={}, commentId={}", userId, commentId);

        userClient.getUserById(userId);
        Comment comment = findCommentById(commentId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("Нельзя удалить чужой комментарий");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Удаление комментария администратором: commentId={}", commentId);

        Comment comment = findCommentById(commentId);
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsForEvent(CommentGetParam param) {
        log.info("Запрос комментариев для события eventId={}", param.getEventId());

        eventClient.getEventById(param.getEventId());

        Specification<Comment> spec = Specification.where(byEventId(param.getEventId()));

        if (param.getAuthorIds() != null && !param.getAuthorIds().isEmpty()) {
            spec = spec.and(byAuthorIds(param.getAuthorIds()));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        if (param.getSortBy() != null) {
            sort = switch (param.getSortBy()) {
                case CREATED -> Sort.by(Sort.Direction.DESC, "created");
                case AUTHOR -> Sort.by("authorId");
            };
        }

        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize(), sort);
        return commentRepository.findAll(spec, pageable).stream()
                .map(this::enrichCommentDto)
                .toList();
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundResource("Комментарий с id " + commentId + " не найден"));
    }

    private CommentDto enrichCommentDto(Comment comment) {
        UserDto author = userClient.getUserById(comment.getAuthorId());
        EventFullDto eventFull = eventClient.getEventById(comment.getEventId());
        EventShortDto eventShort = mapEventFullToShort(eventFull);

        CommentDto dto = commentMapper.toDto(comment);
        dto.setAuthor(author);
        dto.setEvent(eventShort);
        return dto;
    }

    private EventShortDto mapEventFullToShort(EventFullDto full) {
        return EventShortDto.builder()
                .id(full.id())
                .annotation(full.annotation())
                .category(full.category())
                .confirmedRequests(full.confirmedRequests())
                .eventDate(full.eventDate())
                .initiator(full.initiator())
                .paid(full.paid())
                .title(full.title())
                .views(0L) // поле отсутствует в EventFullDto, устанавливаем 0
                .build();
    }

    private Specification<Comment> byEventId(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("eventId"), eventId);
    }

    private Specification<Comment> byAuthorIds(List<Long> authorIds) {
        return (root, query, cb) -> root.get("authorId").in(authorIds);
    }
}