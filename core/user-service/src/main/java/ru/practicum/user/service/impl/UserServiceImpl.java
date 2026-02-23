package ru.practicum.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.exception.ConflictResource;
import ru.practicum.exception.NotFoundResource;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;
import ru.practicum.user.util.UserGetParam;

import java.util.List;

/**
 * Реализация сервиса для работы с пользователями.
 * <p>
 * Обеспечивает бизнес-логику управления пользователями.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    /**
     * Получает перечень пользователей с учетом параметров фильтрации и пагинации.
     *
     * @param userGetParam параметры запроса (фильтрация и пагинация)
     * @return список DTO пользователей
     */
    @Override
    public List<UserDto> getUsers(UserGetParam userGetParam) {
        log.info("Getting users with params: ids={}, from={}, size={}",
                userGetParam.getIds(), userGetParam.getFrom(), userGetParam.getSize());

        List<User> users;

        if (userGetParam.getIds() != null && !userGetParam.getIds().isEmpty()) {
            users = userRepository.findAllByIdIn(userGetParam.getIds());
        } else {
            Pageable pageable = PageRequest.of(userGetParam.getFrom() / userGetParam.getSize(), userGetParam.getSize());
            users = userRepository.findAll(pageable).getContent();
        }

        return users.stream()
                .map(UserMapper::mapToDto)
                .toList();
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return сущность пользователя
     * @throws NotFoundResource если пользователь не найден
     */
    @Override
    public User getUserById(Long userId) {
        log.info("Getting user by id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundResource("Пользователь с id=" + userId + " не найден"));
    }

    /**
     * Получает DTO пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return DTO пользователя
     * @throws NotFoundResource если пользователь не найден
     */
    @Override
    public UserDto getUserDtoById(Long userId) {
        log.info("Getting user DTO by id: {}", userId);
        User user = getUserById(userId);
        return UserMapper.mapToDto(user);
    }

    /**
     * Создает нового пользователя.
     *
     * @param newUserRequest данные для создания пользователя
     * @return DTO созданного пользователя
     * @throws ConflictResource если пользователь с таким email уже существует
     */
    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Creating new user: {}", newUserRequest);

        userRepository.findByEmailContainingIgnoreCase(newUserRequest.getEmail())
                .ifPresent(user -> {
                    throw new ConflictResource("Пользователь с email '" + newUserRequest.getEmail() + "' уже существует");
                });

        User user = UserMapper.mapToUser(newUserRequest);
        User savedUser = userRepository.save(user);

        log.info("Created user with id: {}", savedUser.getId());
        return UserMapper.mapToDto(savedUser);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundResource если пользователь не найден
     */
    @Override
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundResource("Пользователь с id=" + userId + " не найден");
        }

        userRepository.deleteById(userId);
        log.info("Deleted user with id: {}", userId);
    }
}