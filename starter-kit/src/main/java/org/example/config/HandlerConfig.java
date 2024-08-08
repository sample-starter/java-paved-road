package org.example.config;

import lombok.AllArgsConstructor;
import org.example.handlers.*;
import org.example.utils.GitHubUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class HandlerConfig {

    CommonConfig commonConfig;
    GitHubUtil gitHubUtil;
    TemplateConfig templateConfig;

    @Bean
    public Handler exceptionHandler() {
        return new ExceptionHandler(commonConfig);
    }

    @Bean
    public Handler successHandler() {
        return new SuccessHandler(commonConfig);
    }

    @Bean
    public Handler cicdHandler() {
        return new CICDHandler(successHandler(), gitHubUtil);
    }

    @Bean
    public Handler codeCreationHandler() {
        return new CodeCreationHandler(cicdHandler(), gitHubUtil, templateConfig);
    }

    @Bean
    public Handler initializerHandler() {
        return new InitializerHandler(codeCreationHandler(), gitHubUtil);
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
