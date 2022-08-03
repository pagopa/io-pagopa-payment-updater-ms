package it.gov.pagopa.paymentupdater.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.serializer.DeserializationException;

import it.gov.pagopa.paymentupdater.exception.AvroDeserializerException;
import it.gov.pagopa.paymentupdater.exception.SkipDataException;
import it.gov.pagopa.paymentupdater.util.PaymentUtil;
import it.gov.pagopa.paymentupdater.util.TelemetryCustomEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
final class KafkaErrorHandler implements ContainerAwareErrorHandler {

    @Override
    public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer,
            MessageListenerContainer container) {
        doSeeks(records, consumer);
        if (!records.isEmpty()) {
            ConsumerRecord<?, ?> record = records.get(0);
            String topic = record.topic();
            long offset = record.offset();
            int partition = record.partition();
            String message = "";
            if (thrownException.getClass().equals(DeserializationException.class)) {
                DeserializationException exception = (DeserializationException) thrownException;
                message = new String(exception.getData());
                log.info("Skipping message with topic {} and offset {} " +
                        "- malformed message: {} , exception: {}", topic, offset, message,
                        exception.getLocalizedMessage());
                handleErrorMessage(exception.getData());
                return;
            }
            if (thrownException.getClass().equals(AvroDeserializerException.class)) {
                AvroDeserializerException exception = (AvroDeserializerException) thrownException;
                message = new String(exception.getData());
                log.info("Skipping message with topic {} and offset {} " +
                        "- malformed message: {} , exception: {}", topic, offset, message,
                        exception.getLocalizedMessage());
                handleErrorMessage(exception.getData());
                return;
            }
            if (thrownException.getClass().equals(SkipDataException.class)) {
                log.info("Skipping message with topic {} and offset {} " +
                        "- exception: {}", topic, offset,
                        thrownException.getLocalizedMessage());
                return;
            }

            log.info("Skipping message with topic {} - offset {} - partition {} - exception {}", topic, offset,
                    partition, thrownException);

        } else {
            log.info("Consumer exception - cause: {}", thrownException.getMessage());
        }
    }

    private void handleErrorMessage(byte[] bytes) {
        try {
            String message = new String(bytes, StandardCharsets.UTF_8);
            TelemetryCustomEvent.writeTelemetry("ErrorDeserializingMessage", new HashMap<>(),
                    PaymentUtil.getErrorMap(message));

        } catch (Exception e1) {
            log.error(e1.getMessage());
        }
    }

    private void doSeeks(List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer) {
        Map<TopicPartition, Long> partitions = new LinkedHashMap<>();
        AtomicBoolean first = new AtomicBoolean(true);
        records.forEach(record -> {
            if (first.get()) {
                partitions.put(new TopicPartition(record.topic(), record.partition()), record.offset() + 1);
            } else {
                partitions.computeIfAbsent(new TopicPartition(record.topic(), record.partition()),
                        offset -> record.offset());
            }
            first.set(false);
        });
        partitions.forEach(consumer::seek);
    }
}