package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
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
import ru.practicum.user.utill.UserGetParam;

import java.util.List;

/**
 * Реализация сервиса для работы с пользователями.
 * <p>
 * Обеспечивает бизнес-логику управления пользователями.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;

    /**
     * Получает перечень пользователей с учетом параметров фильтрации и пагинации.
     *
     * @param userGetParam параметры запроса (фильтрация и пагинация)
     * @return список DTO пользователей
     */
    @Override
    public List<UserDto> getUsers(UserGetParam userGetParam) {
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
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundResource("Пользователь с id=" + userId + " не найден"));
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
        userRepository.findByEmailContainingIgnoreCase(newUserRequest.getEmail())
                .ifPresent(user -> {
                    throw new ConflictResource("Пользователь с email '" + newUserRequest.getEmail() + "' уже существует");
                });

        User user = UserMapper.mapToUser(newUserRequest);
        User savedUser = userRepository.save(user);

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
        if (!userRepository.existsById(userId)) {
            throw new NotFoundResource("Пользователь с id=" + userId + " не найден");
        }

        userRepository.deleteById(userId);
    }
}