package com.neogamin.proyecto_formativo.compartido.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.usuario.aplicacion.UsuarioDetallesServicio;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.Sesion;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.SesionRepositorioJpa;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioDetallesServicio usuarioDetallesServicio;

    @Mock
    private SesionRepositorioJpa sesionRepositorioJpa;

    @Mock
    private HashTokenServicio hashTokenServicio;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectInvalidTokenWithUnauthorizedOnProtectedEndpoint() throws Exception {
        var filter = buildFilter();
        var request = new MockHttpServletRequest();
        request.setRequestURI("/api/pedidos");
        request.addHeader("Authorization", "Bearer invalid-token");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.extraerUsername("invalid-token")).thenThrow(new JwtException("bad token"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldAuthenticateWhenTokenAndSessionAreValid() throws Exception {
        var filter = buildFilter();
        var request = new MockHttpServletRequest();
        request.setRequestURI("/api/pedidos");
        request.addHeader("Authorization", "Bearer valid-token");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        var usuario = activeUser();

        when(jwtService.extraerUsername("valid-token")).thenReturn(usuario.getEmail());
        when(jwtService.extraerSessionId("valid-token")).thenReturn("session-123");
        when(usuarioDetallesServicio.loadUserByUsername(usuario.getEmail())).thenReturn(usuario);
        when(hashTokenServicio.sha256("session-123")).thenReturn("hashed-session");
        when(sesionRepositorioJpa.findActivaByTokenHash(eq("hashed-session"), any())).thenReturn(Optional.of(new Sesion()));
        when(jwtService.esTokenValido("valid-token", usuario)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(usuario.getEmail());
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldPassThroughWhenAuthorizationHeaderIsMissing() throws Exception {
        var filter = buildFilter();
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreInvalidTokenOnPublicEndpoint() throws Exception {
        var filter = buildFilter();
        var request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        request.addHeader("Authorization", "Bearer invalid-token");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.extraerUsername("invalid-token")).thenThrow(new JwtException("bad token"));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreRevokedTokenOnPublicEndpoint() throws Exception {
        var filter = buildFilter();
        var request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/logout");
        request.addHeader("Authorization", "Bearer revoked-token");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        var usuario = activeUser();

        when(jwtService.extraerUsername("revoked-token")).thenReturn(usuario.getEmail());
        when(jwtService.extraerSessionId("revoked-token")).thenReturn("session-123");
        when(usuarioDetallesServicio.loadUserByUsername(usuario.getEmail())).thenReturn(usuario);
        when(hashTokenServicio.sha256("session-123")).thenReturn("hashed-session");
        when(sesionRepositorioJpa.findActivaByTokenHash(eq("hashed-session"), any())).thenReturn(Optional.empty());

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verify(jwtService, never()).esTokenValido("revoked-token", usuario);
    }

    private JwtAuthenticationFilter buildFilter() {
        return new JwtAuthenticationFilter(
                jwtService,
                usuarioDetallesServicio,
                sesionRepositorioJpa,
                hashTokenServicio
        );
    }

    private Usuario activeUser() {
        var usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNombre("Demo");
        usuario.setEmail("demo@neogaming.com");
        usuario.setPasswordHash("hash");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoGenerico.ACTIVO);
        return usuario;
    }
}
