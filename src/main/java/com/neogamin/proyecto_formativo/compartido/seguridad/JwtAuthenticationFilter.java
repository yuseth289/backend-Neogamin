package com.neogamin.proyecto_formativo.compartido.seguridad;

import com.neogamin.proyecto_formativo.usuario.aplicacion.UsuarioDetallesServicio;
import com.neogamin.proyecto_formativo.usuario.infraestructura.SesionRepositorioJpa;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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

        var token = header.substring(7);
        var username = jwtService.extraerUsername(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var usuario = usuarioDetallesServicio.loadUserByUsername(username);
            var sessionId = jwtService.extraerSessionId(token);
            var tokenHash = hashTokenServicio.sha256(sessionId);
            var sesionActiva = sesionRepositorioJpa.findActivaByTokenHash(tokenHash, OffsetDateTime.now()).isPresent();
            if (sesionActiva && jwtService.esTokenValido(token, usuario)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
