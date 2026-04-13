package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoStockRepositorio extends JpaRepository<com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad, Long> {

    @Modifying
    @Query(value = """
            insert into producto_stock_movimiento (
                fk_producto,
                fk_pedido,
                tipo_movimiento,
                cantidad,
                stock_fisico_anterior,
                stock_fisico_nuevo,
                stock_reservado_anterior,
                stock_reservado_nuevo,
                motivo,
                fecha_movimiento
            ) values (
                :productoId,
                null,
                'ajuste',
                :cantidad,
                :stockFisicoAnterior,
                :stockFisicoNuevo,
                :stockReservadoAnterior,
                :stockReservadoNuevo,
                :motivo,
                :fechaMovimiento
            )
            """, nativeQuery = true)
    void registrarAjusteStock(
            @Param("productoId") Long productoId,
            @Param("cantidad") Integer cantidad,
            @Param("stockFisicoAnterior") Integer stockFisicoAnterior,
            @Param("stockFisicoNuevo") Integer stockFisicoNuevo,
            @Param("stockReservadoAnterior") Integer stockReservadoAnterior,
            @Param("stockReservadoNuevo") Integer stockReservadoNuevo,
            @Param("motivo") String motivo,
            @Param("fechaMovimiento") OffsetDateTime fechaMovimiento
    );
}
