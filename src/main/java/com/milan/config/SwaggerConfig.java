package com.milan.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        OpenAPI openApi = new OpenAPI();

        // API Basic information displayed on the Swagger UI
        Info info = new Info();
        info.setTitle("Milan's Store API");
        info.setDescription("All Api of Milan's Store");
        info.setVersion("1.0.0");
        info.setTermsOfService("http://milanstore.com");
        info.setContact(new Contact().email("lamameelan32@gmail.com").name("Milan Tamang").url("http://milanstore.com/contact"));
        info.setLicense(new License().name("Milan-Store 1.0").url("http://milanstore.com"));

        // Define servers for different environments
        List<Server> serverList = List.of(
                new Server().description("Dev").url("http://localhost:8080")
//                new Server().description("Test").url("http://localhost:8081"),
//                new Server().description("Prod").url("http://localhost:8083")
        );

        // Define JWT-based security scheme using Bearer tokens
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")  // Header name
                .scheme("bearer")       // Type of auth scheme
                .type(SecurityScheme.Type.HTTP)  // HTTP type scheme
                .bearerFormat("JWT")      // Optional: specifies the format as JWT (for UI clarity)
                .in(SecurityScheme.In.HEADER);  // The token is sent in the request header

        // Apply security requirements globally to all endpoints
        Components component = new Components()
                .addSecuritySchemes("Token", securityScheme);

        // Set info, servers, components, and security to OpenAPI object
        openApi.setServers(serverList);
        openApi.setInfo(info);
        openApi.setComponents(component);
        openApi.setSecurity(List.of(new SecurityRequirement().addList("Token")));

        return openApi; // Return configured OpenAPI instance
    }
}
