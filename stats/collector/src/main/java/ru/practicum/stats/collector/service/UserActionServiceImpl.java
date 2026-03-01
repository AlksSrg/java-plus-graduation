package ru.practicum.stats.collector.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.stats.collector.config.CollectorConfig;
import ru.practicum.stats.collector.kafka.KafkaClient;

import java.time.Instant;

import static ru.practicum.ewm.stats.avro.ActionTypeAvro.*;

@Slf4j
@Service
public class UserActionServiceImpl implements UserActionService {

    private final String topic;
    private final Producer<Long, SpecificRecordBase> producer;
    private final KafkaClient kafkaClient;

    public UserActionServiceImpl(KafkaClient kafkaClient,
                                 CollectorConfig config) {
        this.topic = config.getTopic().getUserActions();
        this.producer = kafkaClient.getProducer();
        this.kafkaClient = kafkaClient;

        log.info("UserActionServiceImpl инициализирован. Топик: {}", topic);
    }

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получен запрос: userId={}, eventId={}, actionType={}",
                    request.getUserId(), request.getEventId(), request.getActionType());

            // Отправляем в Kafka
            sendMessage(request);

            // Возвращаем ответ клиенту
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

            log.debug("Запрос успешно обработан");
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    private void sendMessage(UserActionProto request) {
        log.debug("Преобразование сообщения в Avro формат");

        UserActionAvro actionAvro = UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(switch (request.getActionType()) {
                    case ACTION_VIEW -> VIEW;
                    case ACTION_REGISTER -> REGISTER;
                    case ACTION_LIKE -> LIKE;
                    case UNRECOGNIZED -> {
                        log.warn("Получен неизвестный тип действия: {}", request.getActionType());
                        yield null;
                    }
                })
                .setTimestamp(Instant.ofEpochSecond(
                        request.getTimestamp().getSeconds(),
                        request.getTimestamp().getNanos()))
                .build();

        if (actionAvro.getActionType() == null) {
            log.error("Не удалось определить тип действия, сообщение не будет отправлено");
            return;
        }

        log.info("Отправка в Kafka: топик={}, userId={}, eventId={}, actionType={}",
                topic, actionAvro.getUserId(), actionAvro.getEventId(), actionAvro.getActionType());

        ProducerRecord<Long, SpecificRecordBase> record =
                new ProducerRecord<>(topic, actionAvro.getEventId(), actionAvro);

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