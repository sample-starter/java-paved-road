package org.example.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {


    public void process(ConsumerRecord<String, String> consumerRecord) {
        // write custom processing logic here
        log.info("Service logic for processing required!!");
    }
}
