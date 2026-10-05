package com.nbsh.commerceapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH =
            "bearerAuth";

    @Bean
    public OpenAPI commerceOpenApi() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "Commerce API"
                                )
                                .version(
                                        "v1"
                                )
                                .description(
                                        """
                                        Portfolio e-commerce backend built with
                                        Java, Spring Boot, PostgreSQL, Redis,
                                        JWT authentication, transactional checkout,
                                        inventory concurrency control, payments,
                                        idempotency, reviews, and observability.
                                        """
                                )
                                .contact(
                                        new Contact()
                                                .name(
                                                        "Commerce API"
                                                )
                                )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        BEARER_AUTH,
                                        new SecurityScheme()
                                                .type(
                                                        SecurityScheme.Type.HTTP
                                                )
                                                .scheme(
                                                        "bearer"
                                                )
                                                .bearerFormat(
                                                        "JWT"
                                                )
                                )
                );
    }
}