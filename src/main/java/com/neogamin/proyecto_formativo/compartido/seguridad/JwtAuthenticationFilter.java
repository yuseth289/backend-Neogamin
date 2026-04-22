package com.neogamin.proyecto_formativo.compartido.seguridad;

import com.neogamin.proyecto_formativo.usuario.aplicacion.UsuarioDetallesServicio;
import com.neogamin.proyecto_formativo.usuario.infraestructura.SesionRepositorioJpa;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.springframework.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final RequestMatcher PUBLIC_ENDPOINTS_MATCHER = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern("/api/auth/**"),
            PathPatternRequestMatcher.pathPattern("/actuator/health"),
            PathPatternRequestMatcher.pathPattern("/actuator/info"),
            PathPatternRequestMatcher.pathPattern("/swagger-ui.html"),
            PathPatternRequestMatcher.pathPattern("/swagger-ui/**"),
            PathPatternRequestMatcher.pathPattern("/v3/api-docs/**"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/catalogo/productos/**")
    );

    private final JwtService jwtService;
    private final UsuarioDetallesServicio usuarioDetallesServicio;
    private final SesionRepositorioJpa sesionRepositorioJpa;
    private final HashTokenServicio hashTokenServicio;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var token = header.substring(7);
            var username = jwtService.extraerUsername(token);
            if (username == null || username.isBlank()) {
                handleInvalidToken(request, response, filterChain);
                return;
            }
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var usuario = usuarioDetallesServicio.loadUserByUsername(username);
                var sessionId = jwtService.extraerSessionId(token);
                if (sessionId == null || sessionId.isBlank()) {
                    handleInvalidToken(request, response, filterChain);
                    return;
                }
                var tokenHash = hashTokenServicio.sha256(sessionId);
                var sesionActiva = sesionRepositorioJpa.findActivaByTokenHash(tokenHash, OffsetDateTime.now()).isPresent();
                if (!sesionActiva || !jwtService.esTokenValido(token, usuario)) {
                    handleInvalidToken(request, response, filterChain);
                    return;
                }

                var authToken = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            handleInvalidToken(request, response, filterChain);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void handleInvalidToken(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        if (PUBLIC_ENDPOINTS_MATCHER.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido o expirado");
    }
}
