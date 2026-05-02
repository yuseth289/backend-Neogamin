package com.neogamin.proyecto_formativo.usuario.aplicacion;

import com.neogamin.proyecto_formativo.compartido.aplicacion.UnauthorizedException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.compartido.seguridad.HashTokenServicio;
import com.neogamin.proyecto_formativo.compartido.seguridad.JwtService;
import com.neogamin.proyecto_formativo.compartido.seguridad.SecurityProperties;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.UsuarioInicioSesionEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.UsuarioRegistradoEmailEvent;
import com.neogamin.proyecto_formativo.usuario.api.dto.LoginRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.LoginResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.RegistroUsuarioRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.UsuarioResponse;
import io.jsonwebtoken.JwtException;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.Sesion;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.SesionRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutenticacionServicio {

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final SesionRepositorioJpa sesionRepositorioJpa;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;
    private final HashTokenServicio hashTokenServicio;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public UsuarioResponse registrar(RegistroUsuarioRequest request) {
        String emailNormalizado = normalizarEmail(request.email());
        if (usuarioRepositorioJpa.findByEmailIgnoreCase(emailNormalizado).isPresent()) {
            throw new BadRequestException("Ya existe un usuario registrado con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setTelefono(request.telefono() != null && !request.telefono().isBlank()
                ? request.telefono().trim()
                : null);
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoGenerico.ACTIVO);

        Usuario usuarioGuardado = usuarioRepositorioJpa.save(usuario);
        applicationEventPublisher.publishEvent(new UsuarioRegistradoEmailEvent(
                usuarioGuardado.getId(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getEmail()
        ));
        return new UsuarioResponse(
                usuarioGuardado.getId(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getEmail(),
                usuarioGuardado.getTelefono(),
                usuarioGuardado.getNumeroDocumento(),
                usuarioGuardado.getRol().name(),
                usuarioGuardado.getEstado().name()
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        var usuario = usuarioRepositorioJpa.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        var ahora = OffsetDateTime.now();
        sesionRepositorioJpa.revokeAllByUsuario(usuario.getId(), ahora);
        var sessionId = UUID.randomUUID().toString();
        var token = jwtService.generarToken(usuario, sessionId);

        var sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setTokenHash(hashTokenServicio.sha256(sessionId));
        sesion.setIpOrigen(servletRequest.getRemoteAddr());
        sesion.setUserAgent(servletRequest.getHeader("User-Agent"));
        sesion.setActiva(true);
        sesion.setCreadaEn(ahora);
        sesion.setExpiraEn(ahora.plusMinutes(securityProperties.expiracionMinutos()));
        sesionRepositorioJpa.save(sesion);
        applicationEventPublisher.publishEvent(new UsuarioInicioSesionEmailEvent(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        ));

        return new LoginResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                "ROLE_" + usuario.getRol().name()
        );
    }

    @Transactional
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }

        try {
            var token = authorizationHeader.substring(7);
            var sessionId = jwtService.extraerSessionId(token);
            if (sessionId == null || sessionId.isBlank()) {
                return;
            }

            sesionRepositorioJpa.revokeByTokenHash(hashTokenServicio.sha256(sessionId), OffsetDateTime.now());
        } catch (JwtException | IllegalArgumentException ex) {
            return;
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
