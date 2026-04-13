package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ProductoRepositorio extends JpaRepository<ProductoEntidad, Long>, JpaSpecificationExecutor<ProductoEntidad> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductoEntidad p where p.id = :id")
    Optional<ProductoEntidad> findByIdParaActualizacion(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"categoria", "vendedor", "imagenes"})
    Page<ProductoEntidad> findAll(Specification<ProductoEntidad> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"categoria", "vendedor", "imagenes"})
    Optional<ProductoEntidad> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"categoria", "vendedor", "imagenes"})
    Optional<ProductoEntidad> findBySlugIgnoreCaseAndDeletedAtIsNull(String slug);
}
