package ru.practicum.comment.service;

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
import ru.practicum.comment.utill.CommentGetParam;
import ru.practicum.event.service.EventService;
import ru.practicum.event.utill.State;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.ForbiddenResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.user.service.UserService;

import java.util.List;

/**
 * Реализация сервиса для управления комментариями к событиям.
 * Предоставляет функциональность для создания, получения, обновления и удаления комментариев.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImp implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;

    /**
     * Получает комментарий по идентификаторам пользователя и комментария.
     * Проверяет права доступа - только автор может просматривать свой комментарий.
     *
     * @param userId    идентификатор пользователя, должен быть положительным
     * @param commentId идентификатор комментария, должен быть положительным
     * @return DTO комментария
     * @throws NotFoundResource  если комментарий с указанным ID не найден
     * @throws ForbiddenResource если пользователь не является автором комментария
     */
    @Override
    public CommentDto get(long userId, long commentId) {
        userService.getUserById(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Комментарий с id = %d не найден", commentId)));

        if (comment.getAuthor().getId() != userId) {
            throw new ForbiddenResource("Просмотр комментария другого автора невозможен");
        }

        return CommentMapper.mapFromComment(comment);
    }

    /**
     * Получает все комментарии указанного пользователя.
     *
     * @param userId идентификатор пользователя, должен быть положительным
     * @return список DTO комментариев пользователя, может быть пустым
     */
    @Override
    public List<CommentDto> getAll(long userId) {
        userService.getUserById(userId);

        return commentRepository.findAllByAuthorId(userId).stream()
                .map(CommentMapper::mapFromComment)
                .toList();
    }

    /**
     * Создает новый комментарий к событию.
     * Проверяет возможность комментирования (событие должно быть опубликовано,
     * пользователь не должен иметь существующего комментария к этому событию).
     *
     * @param comment DTO с данными для создания комментария
     * @return созданный DTO комментария
     * @throws ConflictResource если событие не опубликовано или комментарий уже существует
     */
    @Override
    @Transactional
    public CommentDto create(NewCommentDto comment) {
        if (commentRepository.existsByAuthorIdAndEventId(comment.getAuthor(), comment.getEvent())) {
            throw new ConflictResource("Пользователь уже оставил комментарий к данному событию");
        }

        comment.setEventObj(eventService.getEventById(comment.getEvent()));
        comment.setAuthorObj(userService.getUserById(comment.getAuthor()));

        if (!comment.getEventObj().getState().equals(State.PUBLISHED)) {
            throw new ConflictResource("Комментировать можно только опубликованное событие");
        }

        Comment newComment = CommentMapper.mapFromNewDto(comment);
        Comment savedComment = commentRepository.save(newComment);

        return CommentMapper.mapFromComment(savedComment);
    }

    /**
     * Обновляет существующий комментарий.
     * Проверяет права доступа - только автор может редактировать комментарий.
     *
     * @param comment DTO с данными для обновления комментария
     * @return обновленный DTO комментария
     * @throws NotFoundResource  если комментарий с указанным ID не найден
     * @throws ForbiddenResource если пользователь не является автором комментария
     */
    @Override
    @Transactional
    public CommentDto update(UpdateCommentDto comment) {
        userService.getUserById(comment.getAuthor());

        Comment existingComment = commentRepository.findById(comment.getCommentId())
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Комментарий с id = %d не найден", comment.getCommentId())));

        if (existingComment.getAuthor().getId() != comment.getAuthor()) {
            throw new ForbiddenResource("Редактирование комментария другого автора невозможно");
        }

        existingComment.setText(comment.getText());
        Comment updatedComment = commentRepository.save(existingComment);

        return CommentMapper.mapFromComment(updatedComment);
    }

    /**
     * Удаляет комментарий.
     * Проверяет права доступа - только автор может удалить комментарий.
     *
     * @param userId    идентификатор пользователя, должен быть положительным
     * @param commentId идентификатор комментария, должен быть положительным
     * @throws NotFoundResource  если комментарий с указанным ID не найден
     * @throws ForbiddenResource если пользователь не является автором комментария
     */
    @Override
    @Transactional
    public void delete(long userId, long commentId) {
        userService.getUserById(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundResource(
                        String.format("Комментарий с id = %d не найден", commentId)));

        if (comment.getAuthor().getId() != userId) {
            throw new ForbiddenResource("Удаление комментария другого автора невозможно");
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
        Sort sort = null;
        Pageable pageable = null;

        eventService.getEventById(param.getEventId());

        if (param.getSortBy() != null) {
            sort = switch (param.getSortBy()) {
                case AUTHOR -> Sort.by("author.name");
                case CREATED -> Sort.by(Sort.Direction.DESC, "created");
            };
        }

        if (sort == null)
            pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
        else
            pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize(), sort);

        Specification<Comment> specification = Specification.where(null);
        specification = specification.and(CommentRepository.byEventId(param.getEventId()));

        if (param.getAuthorIds() != null)
            specification = specification.and(CommentRepository.byAuthor(param.getAuthorIds()));

        return commentRepository.findAll(specification, pageable).stream()
                .map(CommentMapper::mapFromComment)
                .toList();
    }
}