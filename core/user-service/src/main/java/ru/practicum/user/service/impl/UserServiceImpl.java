package ru.practicum.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.util.List;

/**
 * Реализация сервиса для работы с пользователями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        log.info("Создание пользователя: {}", request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictResource("Пользователь с email " + request.getEmail() + " уже существует");
        }

        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        log.info("Пользователь создан с id: {}", saved.getId());

        return userMapper.toDto(saved);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Pageable pageable) {
        log.info("Получение пользователей: ids={}, pageable={}", ids, pageable);

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id")
        );

        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAllWithPagination(sortedPageable);
        } else {
            users = userRepository.findByIdIn(ids, sortedPageable);
        }

        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);
        getUserByIdOrThrow(userId);
        userRepository.deleteById(userId);
        log.info("Пользователь с id {} удалён", userId);
    }

    @Override
    public User getUserById(Long userId) {
        return getUserByIdOrThrow(userId);
    }

    @Override
    public User getUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundResource("Пользователь с id " + userId + " не найден"));
    }

    @Override
    public UserDto getUserDtoById(Long userId) {
        User user = getUserByIdOrThrow(userId);
        return userMapper.toDto(user);
    }

    @Override
    public UserShortDto getUserShortDtoById(Long userId) {
        User user = getUserByIdOrThrow(userId);
        return userMapper.toShortDto(user);
    }
}