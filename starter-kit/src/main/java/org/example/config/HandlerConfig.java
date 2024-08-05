package org.example.config;

import lombok.AllArgsConstructor;
import org.example.handlers.*;
import org.example.utils.GitHubUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class HandlerConfig {

    CommonConfig commonConfig;
    GitHubUtil gitHubUtil;

    @Bean
    public Handler exceptionHandler() {
        return new ExceptionHandler(commonConfig);
    }

    @Bean
    public Handler successHandler() {
        return new SuccessHandler();
    }

    @Bean
    public Handler cicdHandler() {
        return new CICDHandler(successHandler(), gitHubUtil);
    }

    @Bean
    public Handler kafkaHandler() {
        return new KafkaHandler(cicdHandler(), gitHubUtil);
    }

    @Bean
    public Handler apiHandler() {
        return new ApiHandler(kafkaHandler(), gitHubUtil);
    }

    @Bean
    public Handler initializerHandler() {
        return new InitializerHandler(apiHandler(), gitHubUtil);
    }

    @Bean
    public Handler repositoryHandler() {
        return new RepositoryHandler(initializerHandler(), commonConfig);
    }

    @Bean
    public Handler validationHandler() {
        return new ValidationHandler(repositoryHandler());
    }

}
