package com.neogamin.proyecto_formativo.compartido.infraestructura;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguracion {

    public static final String ESQUEMA_SEGURIDAD_BEARER = "bearerAuth";

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Proyecto Formativo")
                        .version("v1")
                        .description("Documentacion OpenAPI del backend ecommerce modular.")
                        .contact(new Contact()
                                .name("Equipo Backend")
                                .email("backend@neogamin.com")))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_SEGURIDAD_BEARER, new SecurityScheme()
                                .name(ESQUEMA_SEGURIDAD_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Autenticacion JWT usando el formato: Bearer <token>")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD_BEARER));
    }

    @Bean
    GroupedOpenApi usuarioOpenApi() {
        return GroupedOpenApi.builder()
                .group("usuario")
                .pathsToMatch("/api/auth/**", "/api/usuarios/**")
                .build();
    }

    @Bean
    GroupedOpenApi catalogoOpenApi() {
        return GroupedOpenApi.builder()
                .group("catalogo")
                .pathsToMatch("/api/catalogo/**")
                .build();
    }

    @Bean
    GroupedOpenApi pedidoOpenApi() {
        return GroupedOpenApi.builder()
                .group("pedido")
                .pathsToMatch("/api/pedidos/**")
                .build();
    }

    @Bean
    GroupedOpenApi inventarioOpenApi() {
        return GroupedOpenApi.builder()
                .group("inventario")
                .pathsToMatch("/api/inventario/**")
                .build();
    }

    @Bean
    GroupedOpenApi pagoOpenApi() {
        return GroupedOpenApi.builder()
                .group("pago")
                .pathsToMatch("/api/pagos/**")
                .build();
    }

    @Bean
    GroupedOpenApi facturacionOpenApi() {
        return GroupedOpenApi.builder()
                .group("facturacion")
                .pathsToMatch("/api/facturas/**")
                .build();
    }

    @Bean
    GroupedOpenApi resenaOpenApi() {
        return GroupedOpenApi.builder()
                .group("resena")
                .pathsToMatch("/api/resenas/**")
                .build();
    }

    @Bean
    GroupedOpenApi interaccionOpenApi() {
        return GroupedOpenApi.builder()
                .group("interaccion")
                .pathsToMatch("/api/interacciones/**", "/api/interaccion/**")
                .build();
    }
}
