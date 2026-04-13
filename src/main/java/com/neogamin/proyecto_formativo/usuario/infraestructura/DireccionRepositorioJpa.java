package com.neogamin.proyecto_formativo.usuario.infraestructura;

import com.neogamin.proyecto_formativo.usuario.dominio.Direccion;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DireccionRepositorioJpa extends JpaRepository<Direccion, Long> {

    Optional<Direccion> findByIdAndUsuarioId(Long id, Long usuarioId);

    Optional<Direccion> findByIdAndUsuarioIdAndEstadoAndDeletedAtIsNull(Long id, Long usuarioId, EstadoGenerico estado);

    List<Direccion> findByUsuarioIdAndEstadoAndDeletedAtIsNullOrderByPrincipalDescCreatedAtDesc(Long usuarioId, EstadoGenerico estado);

    boolean existsByUsuarioIdAndEstadoAndDeletedAtIsNull(Long usuarioId, EstadoGenerico estado);
}
