package com.neogamin.proyecto_formativo.usuario.infraestructura;

import com.neogamin.proyecto_formativo.usuario.dominio.VendedorEntidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendedorRepositorio extends JpaRepository<VendedorEntidad, Long> {

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByNumeroDocumentoIgnoreCase(String numeroDocumento);

    Optional<VendedorEntidad> findByUsuarioId(Long usuarioId);
}
