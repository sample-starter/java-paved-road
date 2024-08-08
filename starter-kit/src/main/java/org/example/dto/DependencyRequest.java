package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DependencyRequest {

    API(0, "api"),
    KAFKA_CONSUMER(1, "kafka-consumer"),
    SQL(2, "sql"),
    KAFKA_PRODUCER(3, "kafka-producer");

    private final int priority;
    private final String templateId;

}
