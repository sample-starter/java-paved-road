package org.example.dto;

import java.time.OffsetDateTime;

public class StarterStep {

    String name;
    OffsetDateTime timestamp;

    public StarterStep(String name, OffsetDateTime timestamp) {
        this.name = name;
        this.timestamp = timestamp;
    }

}
