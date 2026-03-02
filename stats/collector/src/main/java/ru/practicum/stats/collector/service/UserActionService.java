package ru.practicum.stats.collector.service;


import ru.practicum.grpc.stats.action.UserActionProto;

/**
 * Сервис для обработки действий пользователей.
 */
public interface UserActionService {

    /**
     * Принимает действие пользователя, преобразует его в Avro и отправляет в Kafka.
     *
     * @param request действие пользователя в формате Protobuf
     * @throws IllegalArgumentException если тип действия не поддерживается
     */
    void collectUserAction(UserActionProto request);
}