package org.example.kafka.config;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConsumerErrorConfig {

	private static final String HEADER_GROUP_ID = KafkaHeaders.PREFIX + "dlt-group-id";
	private static final String HEADER_ADD_INFO = KafkaHeaders.PREFIX + "dlt-additional-info";
	private static final String ADD_INFO_MESSAGE = "partition: %s, offset: %s";

	@Value("${spring.kafka.consumer.retries:3}")
	private long allowedRetries;
	@Value("${spring.kafka.consumer.retry-interval:1000}")
	private long retryInterval;
	@Value("${spring.kafka.producer.dlq:dlq-topic}")
	private String deadLetterTopic;
	@Value("${spring.kafka.consumer.group-id:consumer-group-id}")
	private String groupID;

	/**
	 * Error handler seek to current error handler.
	 * This gets called everytime a consumer listener has an error while processing
	 * a certain consumer record.
	 *
	 * @param recoverer the recoverer
	 * @return the seek to current error handler
	 */
	@Bean
	public DefaultErrorHandler errorHandler
	(final DeadLetterPublishingRecoverer recoverer) {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer,
				new FixedBackOff(retryInterval, allowedRetries));
		errorHandler.addNotRetryableExceptions(ValueInstantiationException.class);
		return errorHandler;
	}

	/**
	 * Dead-letter publishing recoverer created to publish the failed record to a dlq.
	 * The recoverer will be called after all the retries have been exhausted.
	 * As the messages to be saved in dlt can be of different types we choose the
	 * appropriate kafkatemplate based on the value Type class.
	 *
	 * @param kafkaOperations the kafka operations
	 * @return the dead letter publishing recoverer
	 */
	@Bean
	DeadLetterPublishingRecoverer publishingRecoverer(
			final KafkaOperations<String, String> kafkaOperations) {

		var prodFactory = kafkaOperations.getProducerFactory();
		Map<Class<?>, KafkaOperations<?, ?>> templateMap = new LinkedHashMap<>();
		templateMap.put(String.class, new KafkaTemplate<>(prodFactory,
				Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class)));
		templateMap.put(byte[].class, new KafkaTemplate<>(prodFactory,
				Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class)));
		templateMap.put(Object.class, kafkaOperations);

		var dltRecoverer = new DeadLetterPublishingRecoverer(
				templateMap, (rec, ex) -> {

			logDLQMessage(ex, rec);
			// If the returned TopicPartition has a negative
			// partition, the partition is not set in the
			// ProducerRecord, so the partition is selected by
			// Kafka.
			return new TopicPartition(deadLetterTopic, -1);
		});

		dltRecoverer.setHeadersFunction((rec, ex) ->
				new RecordHeaders(List.of(new RecordHeader(HEADER_GROUP_ID, groupID.getBytes(StandardCharsets.UTF_8)))));

		dltRecoverer.setHeadersFunction((rec, ex) ->
				new RecordHeaders(List.of(new RecordHeader(HEADER_ADD_INFO,
						String.format(ADD_INFO_MESSAGE, rec.partition(), rec.offset()).getBytes(StandardCharsets.UTF_8)))));

		return dltRecoverer;
	}

	private void logDLQMessage(final Exception ex, final ConsumerRecord rec) {
//		LogBuilder.getLogger(log).kv("topic", rec.topic())
//				.kv("partitionKey", rec.partition()).kv("value", rec.value())
//				.kv("patition", rec.partition()).kv("offset", rec.offset())
//				.withCause(ex).error("Exception while processing");
	}
}
