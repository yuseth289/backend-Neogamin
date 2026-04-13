package com.neogamin.proyecto_formativo.resena.infraestructura;

import com.neogamin.proyecto_formativo.resena.dominio.Resena;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResenaRepositorioJpa extends JpaRepository<Resena, Long> {

    @EntityGraph(attributePaths = {"usuario", "producto", "pedido"})
    List<Resena> findByProductoIdAndDeletedAtIsNullOrderByFechaDesc(Long productoId);

    @EntityGraph(attributePaths = {"usuario", "producto", "pedido"})
    Optional<Resena> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"usuario", "producto", "pedido"})
    Optional<Resena> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    @Query(value = """
            select
                cast(coalesce(round(avg(r.calificacion)::numeric, 2), 0) as numeric) as promedioCalificacion,
                count(r.id_resena) as totalResenas,
                coalesce(sum(case when r.calificacion = 5 then 1 else 0 end), 0) as totalCincoEstrellas,
                coalesce(sum(case when r.calificacion = 4 then 1 else 0 end), 0) as totalCuatroEstrellas,
                coalesce(sum(case when r.calificacion = 3 then 1 else 0 end), 0) as totalTresEstrellas,
                coalesce(sum(case when r.calificacion = 2 then 1 else 0 end), 0) as totalDosEstrellas,
                coalesce(sum(case when r.calificacion = 1 then 1 else 0 end), 0) as totalUnaEstrella
            from resena r
            where r.fk_producto = :productoId
              and r.deleted_at is null
            """, nativeQuery = true)
    ResumenResenaProducto resumirPorProducto(@Param("productoId") Long productoId);
}
