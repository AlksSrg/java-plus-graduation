package ru.practicum.ewm.stats.avro.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Универсальный сериализатор для Avro объектов в Kafka.
 * <p>
 * Преобразует объекты, реализующие {@link SpecificRecordBase}, в байтовые массивы
 * для отправки в Kafka топики. Использует Avro схему объекта для сериализации.
 *
 * @param <T> тип Avro объекта для сериализации
 */
public class GeneralAvroSerializer<T extends SpecificRecordBase> implements Serializer<T> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Ничего не настраиваем
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DatumWriter<T> writer = new SpecificDatumWriter<>(data.getSchema());
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null);

            writer.write(data, encoder);
            encoder.flush();

            return out.toByteArray();
        } catch (IOException exp) {
            throw new SerializationException(
                    String.format("Ошибка сериализации данных для топика [%s], тип: %s",
                            topic, data.getClass().getSimpleName()),
                    exp
            );
        }
    }

    @Override
    public void close() {
        // Ничего не закрываем
    }
}