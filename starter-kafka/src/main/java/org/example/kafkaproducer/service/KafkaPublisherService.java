package org.example.kafkaproducer.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Service
public class KafkaPublisherService {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String key, String json, String topic,
                        Map<String, String> headers) {
        try {
            var message = new ProducerRecord<>(topic, key, json);
            Optional.ofNullable(headers)
                    .ifPresent(headerMap -> headerMap.forEach((k, v) -> message.headers().add(k, v.getBytes(
                            StandardCharsets.UTF_8))));
            kafkaTemplate.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
