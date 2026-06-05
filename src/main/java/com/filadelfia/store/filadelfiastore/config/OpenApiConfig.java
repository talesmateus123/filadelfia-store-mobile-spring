package com.filadelfia.store.filadelfiastore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring configuration for OpenAPI documentation.
 *
 * This class creates and exposes an OpenAPI bean used by Swagger UI and
 * other OpenAPI-compatible tools to generate interactive API documentation.
 * It provides metadata such as title, version, contact information, license,
 * and configured server URLs for both development and production environments.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the OpenAPI definition used by the application.
     *
     * @return configured OpenAPI instance for Filadelfia Store
     */
    @Bean
    public OpenAPI filadelfiaStoreOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.filadelfiastore.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setEmail("contact@filadelfiastore.com");
        contact.setName("Filadelfia Store Support");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Filadelfia Store API")
                .version("1.0.0")
                .contact(contact)
                .description("API documentation for Filadelfia Store - E-commerce management system")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}

