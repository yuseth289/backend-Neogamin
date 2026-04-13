package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import com.neogamin.proyecto_formativo.catalogo.dominio.Producto;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductoRepositorioJpa extends JpaRepository<Producto, Long> {

    List<Producto> findByEstadoAndDeletedAtIsNull(EstadoGenerico estado);

    @EntityGraph(attributePaths = {"categoria", "vendedor"})
    Optional<Producto> findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);
}
