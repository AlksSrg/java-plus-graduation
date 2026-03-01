package ru.practicum.ewm.stats.avro.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Сериализатор для UserActionAvro в Kafka.
 */
public class UserActionAvroSerializer implements Serializer<UserActionAvro> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Ничего не настраиваем
    }

    @Override
    public byte[] serialize(String topic, UserActionAvro data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DatumWriter<UserActionAvro> writer = new SpecificDatumWriter<>(UserActionAvro.getClassSchema());
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null);

            writer.write(data, encoder);
            encoder.flush();

            return out.toByteArray();
        } catch (IOException exp) {
            throw new SerializationException(
                    String.format("Ошибка сериализации UserActionAvro для топика [%s]", topic),
                    exp
            );
        }
    }

    @Override
    public void close() {
        // Ничего не закрываем
    }
}