package org.example.config;

import org.example.handlers.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerConfig {

    @Bean
    public Handler exceptionHandler() {
        return new ExceptionHandler();
    }

    @Bean
    public Handler successHandler() {
        return new SuccessHandler();
    }

    @Bean
    public Handler cicdHandler() {
        return new CICDHandler(successHandler());
    }

    @Bean
    public Handler kafkaHandler() {
        return new KafkaHandler(cicdHandler());
    }

    @Bean
    public Handler apiHandler() {
        return new ApiHandler(kafkaHandler());
    }

    @Bean
    public Handler initializerHandler() {
        return new InitializerHandler(apiHandler());
    }

    @Bean
    public Handler repositoryHandler() {
        return new RepositoryHandler(initializerHandler());
    }

    @Bean
    public Handler validationHandler() {
        return new ValidationHandler(repositoryHandler());
    }

}
