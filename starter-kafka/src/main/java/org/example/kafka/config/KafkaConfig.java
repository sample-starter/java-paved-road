package org.example.kafka.config;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.util.Map;

@AllArgsConstructor
@Component
public abstract class KafkaConfig {

    private final KafkaProperties properties;
    private final KafkaSecuredConfig kafkaSecuredConfig;

    protected Map<String, Object> getGenericConsumerConfig() {
        Map<String, Object> consumerConfig = properties.buildConsumerProperties();
        consumerConfig.putAll(kafkaSecuredConfig.getSecuredConfig());

        consumerConfig.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return consumerConfig;
    }

    public abstract Map<String, Object> getConsumerConfig();
}
