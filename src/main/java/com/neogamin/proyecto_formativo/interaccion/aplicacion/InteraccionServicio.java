package com.neogamin.proyecto_formativo.interaccion.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorioJpa;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.interaccion.api.dto.EstadoInteraccionResponse;
import com.neogamin.proyecto_formativo.interaccion.api.dto.WishlistProductoResponse;
import com.neogamin.proyecto_formativo.interaccion.dominio.ProductoDeseado;
import com.neogamin.proyecto_formativo.interaccion.dominio.ProductoLike;
import com.neogamin.proyecto_formativo.interaccion.infraestructura.ProductoDeseadoRepositorioJpa;
import com.neogamin.proyecto_formativo.interaccion.infraestructura.ProductoLikeRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InteraccionServicio {

    private final ProductoLikeRepositorioJpa productoLikeRepositorioJpa;
    private final ProductoDeseadoRepositorioJpa productoDeseadoRepositorioJpa;
    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final ProductoRepositorioJpa productoRepositorioJpa;
    private final InteraccionMapper interaccionMapper;

    @Transactional
    public EstadoInteraccionResponse toggleLike(Long productoId) {
        var usuarioId = SeguridadUtils.usuarioId();
        var producto = productoRepositorioJpa.findById(productoId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        var existente = productoLikeRepositorioJpa.findByUsuarioIdAndProductoId(usuarioId, productoId);
        boolean liked;
        if (existente.isPresent()) {
            productoLikeRepositorioJpa.delete(existente.get());
            liked = false;
        } else {
            var like = new ProductoLike();
            like.setUsuario(usuarioRepositorioJpa.getReferenceById(usuarioId));
            like.setProducto(producto);
            like.setFechaLike(OffsetDateTime.now());
            productoLikeRepositorioJpa.save(like);
            liked = true;
        }
        var deseado = productoDeseadoRepositorioJpa.findByUsuarioIdAndProductoId(usuarioId, productoId).isPresent();
        return new EstadoInteraccionResponse(productoId, liked, deseado);
    }

    @Transactional
    public EstadoInteraccionResponse toggleDeseado(Long productoId) {
        var usuarioId = SeguridadUtils.usuarioId();
        var producto = productoRepositorioJpa.findById(productoId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        var existente = productoDeseadoRepositorioJpa.findByUsuarioIdAndProductoId(usuarioId, productoId);
        boolean deseado;
        if (existente.isPresent()) {
            productoDeseadoRepositorioJpa.delete(existente.get());
            deseado = false;
        } else {
            var item = new ProductoDeseado();
            item.setUsuario(usuarioRepositorioJpa.getReferenceById(usuarioId));
            item.setProducto(producto);
            item.setFechaAgregado(OffsetDateTime.now());
            productoDeseadoRepositorioJpa.save(item);
            deseado = true;
        }
        var liked = productoLikeRepositorioJpa.findByUsuarioIdAndProductoId(usuarioId, productoId).isPresent();
        return new EstadoInteraccionResponse(productoId, liked, deseado);
    }

    @Transactional(readOnly = true)
    public List<WishlistProductoResponse> listarWishlist() {
        return productoDeseadoRepositorioJpa.findByUsuarioIdOrderByFechaAgregadoDesc(SeguridadUtils.usuarioId()).stream()
                .map(interaccionMapper::toWishlistResponse)
                .toList();
    }
}
