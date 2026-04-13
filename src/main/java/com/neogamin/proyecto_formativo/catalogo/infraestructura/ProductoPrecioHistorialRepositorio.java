package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoPrecioHistorialEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoPrecioHistorialRepositorio extends JpaRepository<ProductoPrecioHistorialEntidad, Long> {
}
