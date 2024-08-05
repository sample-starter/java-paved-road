package org.example.kafka.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.List;

/**
 * Base Kafka Consumer Config class which becomes the base class for consumerconfig
 * across all the consumers.
 * The class provides necessary configurations for the ConcurrentKafkaListenerContainerFactory
 * using secured and error-handling beans created from their respective configurations.
 */
@Slf4j
@Import({KafkaConsumerErrorConfig.class, KafkaSecuredConfig.class})
public abstract class BaseKafkaConsumerConfig {

	private static final String GROUP_ID_TAG = "group_id";

	@Autowired
	protected MeterRegistry meterRegistry;
	@Autowired
    DefaultErrorHandler errorHandler;
	@Value("${spring.kafka.consumer.group-id}")
	private String groupID;
    @Value("${LISTENER_CONCURRENCY:1}")
    private String listenerConcurrency;

	/**
	 * Method to generate Kafka Listener Container Factory using the passed-in class.
	 * The class passed-in is used to determine the type of deserializer to be used.
	 * Along with deserializer the method will initialize the factory with a set of
	 * basic properties, metric-listener and error handler.
	 * Generated listener Container Factory is returned from the function allowing the calling
	 * function to further decorate the factory according to any specific requirements.
	 *
	 * @param <T>   the type parameter indicating the consumed value-type.
	 * @param clazz the clazz of the consumerrecord to be used during deserialization.
	 * @return the concurrent kafka listener container factory
	 */
	public <T> ConcurrentKafkaListenerContainerFactory<String, T>
						kafkaListenerContainerFactory(final KafkaConfig config,
                                                      final Class<T> clazz) {

		var consumerFactory = new DefaultKafkaConsumerFactory<>(config.getConsumerConfig(),
				new StringDeserializer(), getDeserializer(clazz));

		addConsumerMeterRegistry(consumerFactory);
		ConcurrentKafkaListenerContainerFactory<String, T> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);
        log.info("Setting consumer concurrency to: {}", parseInteger(listenerConcurrency));
        factory.setConcurrency(parseInteger(listenerConcurrency));
		factory.setRecordInterceptor(consRecord -> {
		    MDC.put("key", consRecord.key());
		    MDC.put("offset", String.valueOf(consRecord.offset()));
		    MDC.put("partition", String.valueOf(consRecord.partition()));
		    return consRecord;
		});
		return factory;
	}

	/**
	 * Determine the ErrorHandlingDeserializer based on the passed-in class type.
	 * Either StringDeserializer or a JsonDeserializer will be used as the
	 * delegating deserializer.
	 *
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	private <T> ErrorHandlingDeserializer<T> getDeserializer(final Class<T> clazz) {
		if(clazz.equals(String.class)) {
			return new ErrorHandlingDeserializer(new StringDeserializer());
		}
		return new ErrorHandlingDeserializer<>(new JsonDeserializer<>(clazz, false));
	}

	/**
	 * Add consumer meter registry.
	 *
	 * @param consumerFactory the consumer factory
	 */
	protected void addConsumerMeterRegistry(final DefaultKafkaConsumerFactory consumerFactory) {
		consumerFactory.addListener(new MicrometerConsumerListener<>(meterRegistry,
				List.of(Tag.of(GROUP_ID_TAG, groupID))));
	}

    private Integer parseInteger(final String concurrency) {
        try {
            return Integer.parseInt(concurrency);
        }
        catch (Exception e) {
            return 1;
        }
    }

}
