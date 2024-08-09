package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
@Getter
public class CommonConfig {

    @Value("${github.token}")
    private String gitToken;

    @Value("${github.apiUrl}")
    private String gitApiUrl;

    @Value("${github.repoPattern}")
    private String repoPattern;

    @Value("${github.starterPattern}")
    private String starterPattern;

    @Value("${github.starterId}")
    private String starterId;

    @Value("${github.templatePath}")
    private String templatePath;

}
