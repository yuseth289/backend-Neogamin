package com.neogamin.proyecto_formativo.compartido.seguridad;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public final class EndpointsPublicos {

    private EndpointsPublicos() {
    }

    public static RequestMatcher requestMatcher() {
        return new OrRequestMatcher(
                PathPatternRequestMatcher.pathPattern("/api/auth/**"),
                PathPatternRequestMatcher.pathPattern("/actuator/health"),
                PathPatternRequestMatcher.pathPattern("/actuator/info"),
                PathPatternRequestMatcher.pathPattern("/swagger-ui.html"),
                PathPatternRequestMatcher.pathPattern("/swagger-ui/**"),
                PathPatternRequestMatcher.pathPattern("/v3/api-docs/**"),
                PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/catalogo/productos/**"),
                PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/catalogo/categorias/**")
        );
    }
}
