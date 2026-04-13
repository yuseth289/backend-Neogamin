package com.neogamin.proyecto_formativo.interaccion.infraestructura;

import com.neogamin.proyecto_formativo.interaccion.dominio.ProductoDeseado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoDeseadoRepositorioJpa extends JpaRepository<ProductoDeseado, Long> {

    Optional<ProductoDeseado> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    @EntityGraph(attributePaths = {"producto", "producto.categoria", "producto.vendedor"})
    List<ProductoDeseado> findByUsuarioIdOrderByFechaAgregadoDesc(Long usuarioId);
}
