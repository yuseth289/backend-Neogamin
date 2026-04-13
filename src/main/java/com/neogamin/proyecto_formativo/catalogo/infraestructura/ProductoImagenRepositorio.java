package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoImagenEntidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoImagenRepositorio extends JpaRepository<ProductoImagenEntidad, Long> {

    boolean existsByProductoIdAndPrincipalTrueAndDeletedAtIsNull(Long productoId);

    List<ProductoImagenEntidad> findByProductoIdAndDeletedAtIsNullOrderByOrdenAsc(Long productoId);

    Optional<ProductoImagenEntidad> findByIdAndProductoIdAndDeletedAtIsNull(Long idImagen, Long productoId);

    Optional<ProductoImagenEntidad> findByProductoIdAndPrincipalTrueAndDeletedAtIsNull(Long productoId);
}
