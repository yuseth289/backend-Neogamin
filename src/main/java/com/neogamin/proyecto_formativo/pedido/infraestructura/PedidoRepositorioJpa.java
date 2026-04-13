package com.neogamin.proyecto_formativo.pedido.infraestructura;

import com.neogamin.proyecto_formativo.pedido.dominio.EstadoPedido;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepositorioJpa extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Optional<Pedido> findWithDetallesById(Long id);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Optional<Pedido> findWithDetallesByNumeroPedido(String numeroPedido);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Page<Pedido> findByUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Page<Pedido> findByUsuarioIdAndEstado(Long usuarioId, EstadoPedido estado, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    Optional<Pedido> findTopByUsuarioIdAndEstadoOrderByFechaCreacionDesc(Long usuarioId, EstadoPedido estado);

    @Query("""
            select p
            from Pedido p
            join p.detalles d
            where p.usuario.id = :usuarioId
              and d.producto.id = :productoId
              and p.estado in :estados
            order by p.fechaCreacion desc
            """)
    List<Pedido> buscarPedidosResenablesPorUsuarioYProducto(
            @Param("usuarioId") Long usuarioId,
            @Param("productoId") Long productoId,
            @Param("estados") Collection<EstadoPedido> estados
    );
}
