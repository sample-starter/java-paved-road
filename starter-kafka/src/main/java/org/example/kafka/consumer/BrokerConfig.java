package org.example.kafka.consumer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.example.kafka.config.KafkaConfig;
import org.example.kafka.config.KafkaSecuredConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class BrokerConfig extends KafkaConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String brokers;

    @Autowired
    public BrokerConfig(KafkaProperties properties, KafkaSecuredConfig kafkaSecuredConfig) {
        super(properties, kafkaSecuredConfig);
    }

    @Override
    public Map<String, Object> getConsumerConfig() {
        Map<String, Object> config = getGenericConsumerConfig();
        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return config;
    }

}
