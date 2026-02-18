package com.nick.npp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NPP Simulation API")
                        .description("REST API for Australia's New Payments Platform (NPP) simulation, " +
                                "covering PayID resolution, real-time payments, PayTo mandates, " +
                                "ISO 20022 messaging, and settlement.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NPP Demo")
                                .email("npp-demo@example.com")));
    }
}