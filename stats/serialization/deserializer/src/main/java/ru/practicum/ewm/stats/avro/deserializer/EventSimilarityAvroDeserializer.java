package ru.practicum.ewm.stats.avro.deserializer;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * Десериализатор для объектов EventSimilarityAvro из Kafka.
 */
public class EventSimilarityAvroDeserializer implements Deserializer<SpecificRecordBase> {

    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final DatumReader<EventSimilarityAvro> reader = new SpecificDatumReader<>(EventSimilarityAvro.getClassSchema());

    /**
     * Десериализует байты в объект EventSimilarityAvro.
     *
     * @param topic название топика
     * @param data байты для десериализации
     * @return объект EventSimilarityAvro
     */
    @Override
    public SpecificRecordBase deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try {
            return reader.read(null, decoderFactory.binaryDecoder(data, null));
        } catch (Exception e) {
            throw new SerializationException("Ошибка десериализации EventSimilarityAvro", e);
        }
    }
}