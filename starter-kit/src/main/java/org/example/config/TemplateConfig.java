package org.example.config;

import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties("templates")
@Setter
public class TemplateConfig {

    private List<StarterConfig> starterList;
    private Map<String, StarterConfig> starterConfigMap;

    @PostConstruct
    void construct() {
        this.starterConfigMap = starterList.stream()
                .collect(Collectors.toMap(fcDetails -> fcDetails.starter, Function.identity()));
    }

    public StarterConfig getStarterConfig(String id) {
        return starterConfigMap.get(id);
    }

    @Data
    public static class StarterConfig {
        String starter;
        List<String> paths;
        String application;
        String target;
        String pom;
        String readme;
    }

}
