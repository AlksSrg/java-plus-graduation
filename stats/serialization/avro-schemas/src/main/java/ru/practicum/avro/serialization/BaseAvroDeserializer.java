package ru.practicum.avro.serialization;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

/**
 * Базовый десериализатор для Avro-сообщений.
 * Использует рефлексию для создания экземпляра целевого класса.
 *
 * @param <T> тип Avro-записи, наследующий SpecificRecordBase
 */
public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final DatumReader<T> datumReader;
    private final Class<T> targetClass;

    /**
     * Конструктор.
     *
     * @param targetClass класс Avro-записи
     * @param schema      схема Avro
     */
    public BaseAvroDeserializer(Class<T> targetClass, Schema schema) {
        this.targetClass = targetClass;
        this.datumReader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            // Создаём пустой экземпляр через рефлексию (требует конструктор по умолчанию)
            T instance = targetClass.getDeclaredConstructor().newInstance();
            return datumReader.read(instance, decoder);
        } catch (IOException e) {
            throw new ru.practicum.avro.serialization.exception.DeserializerException(
                    "Ошибка чтения Avro данных из топика " + topic, e);
        } catch (ReflectiveOperationException e) {
            throw new ru.practicum.avro.serialization.exception.DeserializerException(
                    "Не удалось создать экземпляр класса " + targetClass.getName(), e);
        }
    }

    @Override
    public void close() {
        // ничего не делаем
    }
}