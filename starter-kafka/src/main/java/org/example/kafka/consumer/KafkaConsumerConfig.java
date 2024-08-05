package org.example.kafka.consumer;

import org.example.kafka.config.BaseKafkaConsumerConfig;
import org.example.kafka.config.KafkaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

@EnableKafka
@Configuration

public class KafkaConsumerConfig extends BaseKafkaConsumerConfig {

    @Autowired
    KafkaConfig endgameConfig;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {

        return kafkaListenerContainerFactory(endgameConfig, String.class);
    }

}
