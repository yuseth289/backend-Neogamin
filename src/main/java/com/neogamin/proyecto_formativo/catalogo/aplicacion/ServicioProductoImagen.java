package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.AgregarProductoImagenRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoImagenResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoImagenEntidad;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoImagenRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorio;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioProductoImagen {

    private final ProductoRepositorio productoRepositorio;
    private final ProductoImagenRepositorio productoImagenRepositorio;
    private final ProductoImagenMapper productoImagenMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public ProductoImagenResponse agregarImagen(Long idProducto, AgregarProductoImagenRequest request) {
        var producto = productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));

        validarPuedeGestionarProducto(producto);
        validarOrden(request.orden());
        validarImagenPrincipal(idProducto, request.principal());

        var imagen = new ProductoImagenEntidad();
        imagen.setProducto(producto);
        imagen.setUrlImagen(request.urlImagen().trim());
        imagen.setAltText(request.altText() == null ? null : request.altText().trim());
        imagen.setOrden(request.orden());
        imagen.setPrincipal(request.principal());

        return productoImagenMapper.toResponse(productoImagenRepositorio.save(imagen));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public ProductoImagenResponse cambiarImagenPrincipal(Long idProducto, Long idImagen) {
        var producto = productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));

        validarPuedeGestionarProducto(producto);
        var imagenNuevaPrincipal = productoImagenRepositorio.findByIdAndProductoIdAndDeletedAtIsNull(idImagen, idProducto)
                .orElseThrow(() -> new NotFoundException("La imagen indicada no existe para el producto"));

        productoImagenRepositorio.findByProductoIdAndPrincipalTrueAndDeletedAtIsNull(idProducto)
                .filter(imagenActual -> !imagenActual.getId().equals(idImagen))
                .ifPresent(imagenActual -> imagenActual.setPrincipal(false));

        imagenNuevaPrincipal.setPrincipal(true);
        return productoImagenMapper.toResponse(productoImagenRepositorio.save(imagenNuevaPrincipal));
    }

    private void validarOrden(Integer orden) {
        if (orden == null || orden < 1) {
            throw new BadRequestException("El orden de la imagen debe ser mayor o igual a 1");
        }
    }

    private void validarImagenPrincipal(Long idProducto, Boolean principal) {
        if (Boolean.TRUE.equals(principal)
                && productoImagenRepositorio.existsByProductoIdAndPrincipalTrueAndDeletedAtIsNull(idProducto)) {
            throw new BadRequestException("El producto ya tiene una imagen principal");
        }
    }

    private void validarPuedeGestionarProducto(com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad producto) {
        var usuario = SeguridadUtils.usuarioAutenticado();
        if (usuario.getRol() == RolUsuario.ADMIN) {
            return;
        }
        if (usuario.getRol() == RolUsuario.VENDEDOR
                && producto.getVendedor() != null
                && producto.getVendedor().getId().equals(usuario.getId())) {
            return;
        }
        throw new ForbiddenException("No tienes permisos para gestionar este producto");
    }
}
