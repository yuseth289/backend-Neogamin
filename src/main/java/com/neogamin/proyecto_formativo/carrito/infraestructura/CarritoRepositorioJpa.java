package com.neogamin.proyecto_formativo.carrito.infraestructura;

import com.neogamin.proyecto_formativo.carrito.dominio.CarritoEntidad;
import com.neogamin.proyecto_formativo.carrito.dominio.EstadoCarrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepositorioJpa extends JpaRepository<CarritoEntidad, Long> {

    @EntityGraph(attributePaths = {"usuario", "items", "items.producto"})
    Optional<CarritoEntidad> findByUsuarioIdAndEstado(Long usuarioId, EstadoCarrito estado);
}
