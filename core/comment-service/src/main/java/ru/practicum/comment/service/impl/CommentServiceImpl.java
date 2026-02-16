package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
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
import ru.practicum.event.util.State;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.feignclients.client.EventClient;
import ru.practicum.feignclients.client.UserClient;
import ru.practicum.user.dto.UserDto;

import java.util.List;

/**
 * Реализация сервиса для управления комментариями к событиям.
 * Предоставляет функциональность для создания, получения, обновления и удаления комментариев.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserClient userClient;
    private final EventClient eventClient;

    /**
     * Получает комментарий по идентификаторам пользователя и комментария.
     * Проверяет права доступа - только автор может просматривать свой комментарий.
     *
     * @param userId    идентификатор пользователя, должен быть положительным
     * @param commentId идентификатор комментария, должен быть положительным
     * @return DTO комментария
     * @throws NotFoundResource  если комментарий с указанным ID не найден
     * @throws ForbiddenException если пользователь не является автором комментария
     */
    @Override
    public CommentDto get(Long userId, Long commentId) {
        userClient.getUserById(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Комментарий с id = %d не найден", commentId)));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("Просмотр комментария другого автора невозможен");
        }

        return commentMapper.toDto(comment);
    }

    /**
     * Получает все комментарии указанного пользователя.
     *
     * @param userId идентификатор пользователя, должен быть положительным
     * @return список DTO комментариев пользователя, может быть пустым
     */
    @Override
    public List<CommentDto> getAll(Long userId) {
        userClient.getUserById(userId);

        return commentRepository.findAllByAuthorId(userId).stream()
                .map(commentMapper::toDto)
                .toList();
    }

    /**
     * Создает новый комментарий к событию.
     * Проверяет возможность комментирования (событие должно быть опубликовано,
     * пользователь не должен иметь существующего комментария к этому событию).
     *
     * @param newCommentDto DTO с данными для создания комментария
     * @return созданный DTO комментария
     * @throws ConflictResource если событие не опубликовано или комментарий уже существует
     */
    @Override
    @Transactional
    public CommentDto create(NewCommentDto newCommentDto) {
        // Проверяем, существует ли пользователь
        UserDto author = userClient.getUserById(newCommentDto.getAuthorId());

        // Проверяем, существует ли событие
        EventFullDto event = eventClient.getEventById(newCommentDto.getEventId());

        // Проверяем статус события
        if (!State.PUBLISHED.equals(event.getState())) {
            throw new ConflictResource("Комментировать можно только опубликованное событие");
        }

        // Проверяем, не оставлял ли пользователь уже комментарий к этому событию
        if (commentRepository.existsByAuthorIdAndEventId(newCommentDto.getAuthorId(), newCommentDto.getEventId())) {
            throw new ConflictResource("Пользователь уже оставил комментарий к данному событию");
        }

        Comment newComment = commentMapper.toEntity(newCommentDto, newCommentDto.getAuthorId());
        Comment savedComment = commentRepository.save(newComment);

        return commentMapper.toDto(savedComment);
    }

    /**
     * Обновляет существующий комментарий.
     * Проверяет права доступа - только автор может редактировать комментарий.
     *
     * @param updateCommentDto DTO с данными для обновления комментария
     * @return обновленный DTO комментария
     * @throws NotFoundResource  если комментарий с указанным ID не найден
     * @throws ForbiddenException если пользователь не является автором комментария
     */
    @Override
    @Transactional
    public CommentDto update(UpdateCommentDto updateCommentDto) {
        userClient.getUserById(updateCommentDto.getAuthorId());

        Comment existingComment = commentRepository.findById(updateCommentDto.getCommentId())
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Комментарий с id = %d не найден", updateCommentDto.getCommentId())));

        if (!existingComment.getAuthorId().equals(updateCommentDto.getAuthorId())) {
            throw new ForbiddenException("Редактирование комментария другого автора невозможно");
        }

        commentMapper.updateEntity(existingComment, updateCommentDto);
        Comment updatedComment = commentRepository.save(existingComment);

        return commentMapper.toDto(updatedComment);
    }

    /**
     * Удаляет комментарий.
     * Проверяет права доступа - только автор может удалить комментарий.
     *
     * @param userId    идентификатор пользователя, должен быть положительным
     * @param commentId идентификатор комментария, должен быть положительным
     * @throws NotFoundResource  если комментарий с указанным ID не найден
     * @throws ForbiddenException если пользователь не является автором комментария
     */
    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        userClient.getUserById(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Комментарий с id = %d не найден", commentId)));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("Удаление комментария другого автора невозможно");
        }

        commentRepository.delete(comment);
    }

    /**
     * Получает все комментарии для указанного события.
     * Используется для публичного доступа к комментариям события.
     *
     * @param param параметры выборки
     * @return список DTO комментариев события, может быть пустым
     */
    @Override
    public List<CommentDto> getComments(CommentGetParam param) {
        // Проверяем существование события
        eventClient.getEventById(param.getEventId());

        Sort sort = null;
        Pageable pageable;

        if (param.getSortBy() != null) {
            sort = switch (param.getSortBy()) {
                case AUTHOR -> Sort.by("authorId");
                case CREATED -> Sort.by(Sort.Direction.DESC, "created");
            };
        }

        int page = param.getFrom() / param.getSize();
        if (sort == null) {
            pageable = PageRequest.of(page, param.getSize());
        } else {
            pageable = PageRequest.of(page, param.getSize(), sort);
        }

        Specification<Comment> specification = Specification.where(byEventId(param.getEventId()));

        if (param.getAuthorIds() != null && !param.getAuthorIds().isEmpty()) {
            specification = specification.and(byAuthorIds(param.getAuthorIds()));
        }

        return commentRepository.findAll(specification, pageable).stream()
                .map(commentMapper::toDto)
                .toList();
    }

    private Specification<Comment> byEventId(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("eventId"), eventId);
    }

    private Specification<Comment> byAuthorIds(List<Long> authorIds) {
        return (root, query, cb) -> root.get("authorId").in(authorIds);
    }
}