package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import com.neogamin.proyecto_formativo.catalogo.dominio.MonedaReferenciaEntidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonedaReferenciaRepositorio extends JpaRepository<MonedaReferenciaEntidad, String> {

    boolean existsByCodigoAndActivaTrue(String codigo);

    Optional<MonedaReferenciaEntidad> findByCodigoAndActivaTrue(String codigo);
}
