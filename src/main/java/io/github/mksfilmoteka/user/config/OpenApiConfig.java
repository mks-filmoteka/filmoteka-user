package io.github.mksfilmoteka.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI filmotekaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Filmoteka User API")
                        .version("v1")
                        .description("User profile and film list management API"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Keycloak bearer JWT. User API operations require authentication.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }


    @Bean
    public GroupedOpenApi profileApi() {
        return GroupedOpenApi.builder().group("profile").pathsToMatch("/api/v1/profile/**").build();
    }

    @Bean
    public GroupedOpenApi filmListApi() {
        return GroupedOpenApi.builder().group("film-lists").pathsToMatch("/api/v1/film-lists/**").build();
    }
}
