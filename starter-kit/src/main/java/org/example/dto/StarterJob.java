package org.example.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StarterJob {

    DependencyRequest request;
    List<StarterStep> steps;
    boolean isDone;

    public StarterJob(DependencyRequest request) {
        this.request = request;
        this.steps = new ArrayList<>();
        this.isDone = false;
    }

}
