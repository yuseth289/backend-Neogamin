package com.neogamin.proyecto_formativo.pedido.infraestructura;

import com.neogamin.proyecto_formativo.pedido.dominio.PedidoDetalle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoDetalleRepositorioJpa extends JpaRepository<PedidoDetalle, Long> {

    Optional<PedidoDetalle> findByPedidoIdAndProductoId(Long pedidoId, Long productoId);

    void deleteByPedidoId(Long pedidoId);

    @Query(value = """
            select case when count(pd.id_detalle) > 0 then true else false end
            from pedido_detalle pd
            join pedido p on p.id_pedido = pd.fk_pedido
            where p.fk_usuario = :usuarioId
              and pd.fk_producto = :productoId
              and p.estado in (
                  'pagado'::estado_pedido,
                  'enviado'::estado_pedido,
                  'entregado'::estado_pedido
              )
            """, nativeQuery = true)
    boolean existsCompraVerificada(
            @Param("usuarioId") Long usuarioId,
            @Param("productoId") Long productoId
    );
}
