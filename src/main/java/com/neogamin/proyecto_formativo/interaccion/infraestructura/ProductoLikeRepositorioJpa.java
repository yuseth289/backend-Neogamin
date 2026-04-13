package com.neogamin.proyecto_formativo.interaccion.infraestructura;

import com.neogamin.proyecto_formativo.interaccion.dominio.ProductoLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoLikeRepositorioJpa extends JpaRepository<ProductoLike, Long> {

    Optional<ProductoLike> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
}
