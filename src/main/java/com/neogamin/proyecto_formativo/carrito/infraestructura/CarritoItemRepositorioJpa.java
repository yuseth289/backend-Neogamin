package com.neogamin.proyecto_formativo.carrito.infraestructura;

import com.neogamin.proyecto_formativo.carrito.dominio.CarritoItemEntidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoItemRepositorioJpa extends JpaRepository<CarritoItemEntidad, Long> {

    Optional<CarritoItemEntidad> findByCarritoIdAndProductoId(Long carritoId, Long productoId);

    Optional<CarritoItemEntidad> findByIdAndCarritoId(Long itemId, Long carritoId);

    void deleteByCarritoId(Long carritoId);
}
