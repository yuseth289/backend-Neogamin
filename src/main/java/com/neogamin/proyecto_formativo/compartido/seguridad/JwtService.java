package com.neogamin.proyecto_formativo.compartido.seguridad;

import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final SecurityProperties properties;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
    }

    public String generarToken(Usuario usuario, String sessionId) {
        var ahora = Instant.now();
        var expiracion = ahora.plus(properties.expiracionMinutos(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .claim("sid", sessionId)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(getSigningKey())
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerSessionId(String token) {
        return extraerClaims(token).get("sid", String.class);
    }

    public boolean esTokenValido(String token, Usuario usuario) {
        var claims = extraerClaims(token);
        return claims.getSubject().equals(usuario.getEmail()) && claims.getExpiration().after(new Date());
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(toBase64(properties.secret())));
    }

    private String toBase64(String raw) {
        return java.util.Base64.getEncoder().encodeToString(raw.getBytes());
    }
}
