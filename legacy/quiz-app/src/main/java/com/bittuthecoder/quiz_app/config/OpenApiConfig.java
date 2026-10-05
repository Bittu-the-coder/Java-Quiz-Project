package com.bittuthecoder.quiz_app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI quizAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quiz Application API")
                        .description("Backend APIs for Quiz App")
                        .version("1.0.0"));
    }
}
