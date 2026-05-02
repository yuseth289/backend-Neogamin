package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaActivaResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CrearOfertaRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.OfertaEntidad;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.OfertaRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorio;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioOferta {

    private final OfertaRepositorio ofertaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final OfertaMapper ofertaMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public OfertaResponse crearOferta(CrearOfertaRequest request) {
        var producto = productoRepositorio.findById(request.productoId())
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));

        validarPuedeGestionarProducto(producto);
        validarFechas(request.fechaInicio(), request.fechaFin());
        validarTipoDescuento(request.porcentajeDesc(), request.precioOferta());
        var estado = parsearEstado(request.estado());

        if (estado == EstadoGenerico.ACTIVO
                && ofertaRepositorio.existeOfertaActivaSolapada(producto.getId(), request.fechaInicio(), request.fechaFin())) {
            throw new BadRequestException("Ya existe una oferta activa solapada para este producto");
        }

        var oferta = new OfertaEntidad();
        oferta.setProducto(producto);
        oferta.setTitulo(request.titulo().trim());
        oferta.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
        oferta.setPorcentajeDesc(request.porcentajeDesc());
        oferta.setPrecioOferta(request.precioOferta());
        oferta.setFechaInicio(request.fechaInicio());
        oferta.setFechaFin(request.fechaFin());
        oferta.setEstado(estado);

        var guardada = ofertaRepositorio.save(oferta);
        actualizarPrecioVigenteSiAplica(producto, guardada);
        return ofertaMapper.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<OfertaActivaResponse> listarOfertasActivas() {
        return ofertaRepositorio.findOfertasActivasVigentes(OffsetDateTime.now()).stream()
                .map(ofertaMapper::toActivaResponse)
                .toList();
    }

    private void validarFechas(OffsetDateTime fechaInicio, OffsetDateTime fechaFin) {
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new BadRequestException("La fecha fin debe ser posterior a la fecha inicio");
        }
    }

    private void validarTipoDescuento(BigDecimal porcentajeDesc, BigDecimal precioOferta) {
        var tienePorcentaje = porcentajeDesc != null;
        var tienePrecioOferta = precioOferta != null;
        if (tienePorcentaje == tienePrecioOferta) {
            throw new BadRequestException("Debes enviar exactamente uno entre porcentajeDesc y precioOferta");
        }
    }

    private EstadoGenerico parsearEstado(String estado) {
        try {
            return EstadoGenerico.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El estado indicado no es válido");
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

    private void actualizarPrecioVigenteSiAplica(
            com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad producto,
            OfertaEntidad oferta
    ) {
        var ahora = OffsetDateTime.now();
        if (oferta.getEstado() != EstadoGenerico.ACTIVO || ahora.isBefore(oferta.getFechaInicio()) || !ahora.isBefore(oferta.getFechaFin())) {
            return;
        }

        if (oferta.getPrecioOferta() != null) {
            producto.setPrecioVigenteCache(oferta.getPrecioOferta());
        } else {
            var precioConDescuento = producto.getPrecioLista()
                    .multiply(BigDecimal.ONE.subtract(oferta.getPorcentajeDesc().movePointLeft(2)));
            producto.setPrecioVigenteCache(precioConDescuento);
        }
        productoRepositorio.save(producto);
    }
}
