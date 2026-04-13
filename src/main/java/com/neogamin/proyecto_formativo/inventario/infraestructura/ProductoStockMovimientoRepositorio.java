package com.neogamin.proyecto_formativo.inventario.infraestructura;

import com.neogamin.proyecto_formativo.inventario.dominio.ProductoStockMovimientoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoStockMovimientoRepositorio extends JpaRepository<ProductoStockMovimientoEntidad, Long> {
}
