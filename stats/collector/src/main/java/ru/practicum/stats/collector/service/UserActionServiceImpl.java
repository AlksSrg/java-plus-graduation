package ru.practicum.stats.collector.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.stats.collector.config.CollectorConfig;
import ru.practicum.stats.collector.kafka.KafkaClient;

import java.time.Instant;

/**
 * Реализация сервиса обработки действий пользователей.
 * Преобразует Protobuf-сообщение в Avro и отправляет в Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {

    private final KafkaClient kafkaClient;
    private final CollectorConfig config;

    @Override
    public void collectUserAction(UserActionProto request) {
        log.debug("Преобразование сообщения в Avro формат");

        UserActionAvro avro = buildUserActionAvro(request);
        if (avro == null) {
            log.error("Не удалось создать Avro-сообщение из-за неизвестного типа действия");
            throw new IllegalArgumentException("Unsupported action type: " + request.getActionType());
        }

        sendToKafka(avro);
    }

    /**
     * Преобразует Protobuf-сообщение в Avro.
     */
    private UserActionAvro buildUserActionAvro(UserActionProto request) {
        ActionTypeProto protoType = request.getActionType();
        if (protoType == null) {
            throw new IllegalArgumentException("Action type cannot be null");
        }

        ActionTypeAvro avroType = switch (protoType) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> null;
        };

        if (avroType == null) {
            return null;
        }

        Instant timestamp = Instant.ofEpochSecond(
                request.getTimestamp().getSeconds(),
                request.getTimestamp().getNanos()
        );

        return UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(avroType)
                .setTimestamp(timestamp)
                .build();
    }

    /**
     * Отправляет Avro-сообщение в Kafka.
     */
    private void sendToKafka(UserActionAvro avro) {
        Producer<Long, SpecificRecordBase> producer = kafkaClient.getProducer();
        String topic = config.getTopic().getUserActions();

        log.info("Отправка в Kafka: топик={}, userId={}, eventId={}, actionType={}",
                topic, avro.getUserId(), avro.getEventId(), avro.getActionType());

        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                topic, avro.getEventId(), avro
        );

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке в Kafka: {}", exception.getMessage(), exception);
            } else {
                log.info("Успешно отправлено в Kafka: partition={}, offset={}",
                        metadata.partition(), metadata.offset());
            }
        });
    }

    @PreDestroy
    public void stop() {
        log.info("Завершение работы UserActionServiceImpl");
        kafkaClient.stop();
    }
}