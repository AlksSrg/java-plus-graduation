package ru.yandex.practicum.kafka.serialization;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Serializer;
import ru.yandex.practicum.kafka.serialization.exception.SerializerException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BaseAvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase object) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] result = null;
            if (object != null) {
                BinaryEncoder encoder = encoderFactory.binaryEncoder(outputStream, null);
                DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(object.getSchema());
                writer.write(object, encoder);
                encoder.flush();
                result = outputStream.toByteArray();
            }

            return result;
        } catch (IOException ex) {
            throw new SerializerException("Ошибка сериализации для топика %s".formatted(topic), ex);
        }
    }
}