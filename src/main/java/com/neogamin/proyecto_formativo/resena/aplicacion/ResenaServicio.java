package com.neogamin.proyecto_formativo.resena.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorioJpa;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.pedido.dominio.EstadoPedido;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoRepositorioJpa;
import com.neogamin.proyecto_formativo.resena.api.dto.CrearOActualizarResenaRequest;
import com.neogamin.proyecto_formativo.resena.api.dto.ResenaProductoResponse;
import com.neogamin.proyecto_formativo.resena.api.dto.ResumenCalificacionProductoResponse;
import com.neogamin.proyecto_formativo.resena.dominio.Resena;
import com.neogamin.proyecto_formativo.resena.infraestructura.ResenaRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResenaServicio {

    private static final Set<EstadoPedido> ESTADOS_VALIDOS_PARA_RESENA = Set.of(
            EstadoPedido.PAGADO,
            EstadoPedido.PREPARANDO,
            EstadoPedido.ENVIADO,
            EstadoPedido.ENTREGADO
    );

    private final ResenaRepositorioJpa resenaRepositorioJpa;
    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final ProductoRepositorioJpa productoRepositorioJpa;
    private final PedidoRepositorioJpa pedidoRepositorioJpa;

    @Transactional
    public ResenaProductoResponse crearOActualizar(CrearOActualizarResenaRequest request) {
        validarCalificacion(request.calificacion());

        var usuarioAutenticado = SeguridadUtils.usuarioAutenticado();
        var usuario = usuarioRepositorioJpa.findById(usuarioAutenticado.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        var producto = productoRepositorioJpa.findByIdAndDeletedAtIsNull(request.productoId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        var pedidoAsociado = pedidoRepositorioJpa.buscarPedidosResenablesPorUsuarioYProducto(
                        usuario.getId(),
                        producto.getId(),
                        ESTADOS_VALIDOS_PARA_RESENA
                )
                .stream()
                .findFirst()
                .orElse(null);

        var esAdmin = usuarioAutenticado.getRol() == RolUsuario.ADMIN;
        if (!esAdmin && pedidoAsociado == null) {
            throw new ForbiddenException("No puedes reseñar un producto que no has comprado");
        }

        var compraVerificada = pedidoAsociado != null;
        var resena = resenaRepositorioJpa.findByUsuarioIdAndProductoId(usuario.getId(), producto.getId())
                .orElseGet(Resena::new);

        resena.setUsuario(usuario);
        resena.setProducto(producto);
        resena.setPedido(pedidoAsociado);
        resena.setCompraVerificada(compraVerificada);
        resena.setCalificacion(request.calificacion());
        resena.setComentario(normalizarComentario(request.comentario()));
        resena.setFecha(OffsetDateTime.now());
        resena.setDeletedAt(null);

        return toResponse(resenaRepositorioJpa.save(resena));
    }

    @Transactional(readOnly = true)
    public List<ResenaProductoResponse> listarPorProducto(Long productoId) {
        validarProductoExiste(productoId);
        return resenaRepositorioJpa.findByProductoIdAndDeletedAtIsNullOrderByFechaDesc(productoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumenCalificacionProductoResponse obtenerResumenProducto(Long productoId) {
        validarProductoExiste(productoId);
        var resumen = resenaRepositorioJpa.resumirPorProducto(productoId);
        return new ResumenCalificacionProductoResponse(
                productoId,
                normalizarPromedio(resumen.getPromedioCalificacion()),
                valorSeguro(resumen.getTotalResenas()),
                valorSeguro(resumen.getTotalCincoEstrellas()),
                valorSeguro(resumen.getTotalCuatroEstrellas()),
                valorSeguro(resumen.getTotalTresEstrellas()),
                valorSeguro(resumen.getTotalDosEstrellas()),
                valorSeguro(resumen.getTotalUnaEstrella())
        );
    }

    @Transactional
    public void eliminar(Long resenaId) {
        var resena = resenaRepositorioJpa.findByIdAndDeletedAtIsNull(resenaId)
                .orElseThrow(() -> new NotFoundException("Reseña no encontrada"));

        var usuarioAutenticado = SeguridadUtils.usuarioAutenticado();
        var esAdmin = usuarioAutenticado.getRol() == RolUsuario.ADMIN;
        var esPropietario = resena.getUsuario().getId().equals(usuarioAutenticado.getId());

        if (!esAdmin && !esPropietario) {
            throw new ForbiddenException("No tienes permisos para eliminar esta reseña");
        }

        resena.setDeletedAt(OffsetDateTime.now());
        resenaRepositorioJpa.save(resena);
    }

    private void validarProductoExiste(Long productoId) {
        if (productoRepositorioJpa.findByIdAndDeletedAtIsNull(productoId).isEmpty()) {
            throw new NotFoundException("Producto no encontrado");
        }
    }

    private void validarCalificacion(Short calificacion) {
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new BadRequestException("La calificación debe estar entre 1 y 5 estrellas");
        }
    }

    private String normalizarComentario(String comentario) {
        if (comentario == null) {
            return null;
        }
        var comentarioNormalizado = comentario.trim();
        return comentarioNormalizado.isEmpty() ? null : comentarioNormalizado;
    }

    private BigDecimal normalizarPromedio(BigDecimal promedio) {
        return promedio == null ? BigDecimal.ZERO.setScale(2) : promedio.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private Long valorSeguro(Long valor) {
        return valor == null ? 0L : valor;
    }

    private ResenaProductoResponse toResponse(Resena resena) {
        return new ResenaProductoResponse(
                resena.getId(),
                resena.getProducto().getId(),
                resena.getUsuario().getId(),
                resena.getUsuario().getNombre(),
                resena.getPedido() != null ? resena.getPedido().getId() : null,
                Boolean.TRUE.equals(resena.getCompraVerificada()),
                resena.getCalificacion(),
                resena.getComentario(),
                resena.getFecha()
        );
    }
}
