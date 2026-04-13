package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import com.neogamin.proyecto_formativo.catalogo.dominio.OfertaEntidad;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfertaRepositorio extends JpaRepository<OfertaEntidad, Long> {

    @Query("""
            select o from OfertaEntidad o
            where o.producto.id = :productoId
              and o.estado = com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico.ACTIVO
              and :ahora >= o.fechaInicio
              and :ahora < o.fechaFin
            order by o.fechaInicio desc
            """)
    Optional<OfertaEntidad> findOfertaVigente(@Param("productoId") Long productoId, @Param("ahora") OffsetDateTime ahora);

    @EntityGraph(attributePaths = {"producto"})
    @Query("""
            select o from OfertaEntidad o
            where o.estado = com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico.ACTIVO
              and :ahora >= o.fechaInicio
              and :ahora < o.fechaFin
              and o.producto.deletedAt is null
            order by o.fechaInicio asc, o.id asc
            """)
    List<OfertaEntidad> findOfertasActivasVigentes(@Param("ahora") OffsetDateTime ahora);

    @Query("""
            select case when count(o) > 0 then true else false end
            from OfertaEntidad o
            where o.producto.id = :productoId
              and o.estado = com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico.ACTIVO
              and o.fechaInicio < :fechaFin
              and o.fechaFin > :fechaInicio
            """)
    boolean existeOfertaActivaSolapada(
            @Param("productoId") Long productoId,
            @Param("fechaInicio") OffsetDateTime fechaInicio,
            @Param("fechaFin") OffsetDateTime fechaFin
    );
}
