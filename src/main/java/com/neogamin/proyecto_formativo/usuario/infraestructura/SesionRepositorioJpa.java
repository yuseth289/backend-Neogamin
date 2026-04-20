package com.neogamin.proyecto_formativo.usuario.infraestructura;

import com.neogamin.proyecto_formativo.usuario.dominio.Sesion;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SesionRepositorioJpa extends JpaRepository<Sesion, Long> {

    @Query("""
            select s from Sesion s
            where s.tokenHash = :tokenHash
              and s.activa = true
              and s.expiraEn > :ahora
            """)
    Optional<Sesion> findActivaByTokenHash(@Param("tokenHash") String tokenHash, @Param("ahora") OffsetDateTime ahora);

    @Modifying
    @Query("""
            update Sesion s
               set s.activa = false,
                   s.revocadaEn = :ahora
             where s.usuario.id = :usuarioId
               and s.activa = true
            """)
    void revokeAllByUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") OffsetDateTime ahora);

    @Modifying
    @Query("""
            update Sesion s
               set s.activa = false,
                   s.revocadaEn = :ahora
             where s.tokenHash = :tokenHash
               and s.activa = true
            """)
    void revokeByTokenHash(@Param("tokenHash") String tokenHash, @Param("ahora") OffsetDateTime ahora);
}
