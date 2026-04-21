package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import com.neogamin.proyecto_formativo.catalogo.dominio.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoriaRepositorioJpa extends JpaRepository<Categoria, Long> {

    @Query(value = """
            select *
            from categoria c
            where c.deleted_at is null
              and c.estado = 'activo'
            order by c.nombre asc
            """, nativeQuery = true)
    List<Categoria> findActivasNoEliminadasOrderByNombreAsc();

    Optional<Categoria> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySlugIgnoreCase(String slug);
}
