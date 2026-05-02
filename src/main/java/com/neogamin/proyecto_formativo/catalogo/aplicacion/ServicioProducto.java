package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.ActualizarProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ActualizarPrecioProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ActualizarStockProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CrearProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.FiltroProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoDetalleResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoListadoResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoPrecioHistorialEntidad;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.CategoriaRepositorioJpa;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.MonedaReferenciaRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.MovimientoStockRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.OfertaRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoEspecificacion;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoPrecioHistorialRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorio;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.resena.infraestructura.ResenaRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioProducto {

    private final ProductoRepositorio productoRepositorio;
    private final CategoriaRepositorioJpa categoriaRepositorioJpa;
    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final MonedaReferenciaRepositorio monedaReferenciaRepositorio;
    private final MovimientoStockRepositorio movimientoStockRepositorio;
    private final OfertaRepositorio ofertaRepositorio;
    private final ProductoPrecioHistorialRepositorio productoPrecioHistorialRepositorio;
    private final ResenaRepositorioJpa resenaRepositorioJpa;
    private final ProductoMapper productoMapper;

    @Transactional(readOnly = true)
    public Page<ProductoListadoResponse> listarProductos(FiltroProductoRequest filtro) {
        validarRangoPrecio(filtro.getPrecioMin(), filtro.getPrecioMax());
        var pageable = crearPageable(filtro);
        var estadoFiltro = parsearEstado(filtro.getEstado());

        return productoRepositorio.findAll(
                        ProductoEspecificacion.conFiltros(filtro, estadoFiltro),
                        pageable
                )
                .map(productoMapper::toListadoResponse);
    }

    @Transactional(readOnly = true)
    public ProductoDetalleResponse obtenerDetallePorId(Long idProducto) {
        var producto = productoRepositorio.findByIdAndDeletedAtIsNull(idProducto)
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));
        return construirDetalleProducto(producto);
    }

    @Transactional(readOnly = true)
    public ProductoDetalleResponse obtenerDetallePorSlug(String slug) {
        var producto = productoRepositorio.findBySlugIgnoreCaseAndDeletedAtIsNull(slug.trim())
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));
        return construirDetalleProducto(producto);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public ProductoResponse crearProducto(CrearProductoRequest request) {
        validarSku(request.sku());
        validarSlug(request.slug());
        validarMoneda(request.moneda());

        var categoria = categoriaRepositorioJpa.findById(request.categoriaId())
                .orElseThrow(() -> new NotFoundException("La categoría indicada no existe"));

        var vendedor = usuarioRepositorioJpa.findById(SeguridadUtils.usuarioId())
                .orElseThrow(() -> new NotFoundException("El usuario autenticado no existe"));

        if (vendedor.getRol() != RolUsuario.VENDEDOR && vendedor.getRol() != RolUsuario.ADMIN) {
            throw new BadRequestException("El usuario autenticado no tiene permisos para crear productos");
        }

        var producto = new ProductoEntidad();
        producto.setCategoria(categoria);
        producto.setVendedor(vendedor);
        producto.setSku(normalizar(request.sku()));
        producto.setSlug(normalizarSlug(request.slug()));
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
        producto.setMoneda(request.moneda().trim().toUpperCase());
        producto.setPrecioLista(request.precioLista());
        producto.setPrecioVigenteCache(request.precioLista());
        producto.setStockFisico(request.stockFisico());
        producto.setStockReservado(0);
        producto.setNeedsRecalc(false);
        producto.setCondicion(request.condicion() == null ? null : request.condicion().trim());
        producto.setEstado(EstadoGenerico.ACTIVO);

        var guardado = productoRepositorio.save(producto);
        return productoMapper.toResponse(guardado);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public ProductoResponse actualizarProducto(Long idProducto, ActualizarProductoRequest request) {
        var producto = productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));
        validarPuedeGestionarProducto(producto);
        validarReasignacionVendedor(request.vendedorId());

        var categoria = categoriaRepositorioJpa.findById(request.categoriaId())
                .orElseThrow(() -> new NotFoundException("La categoría indicada no existe"));

        var vendedor = usuarioRepositorioJpa.findById(request.vendedorId())
                .orElseThrow(() -> new NotFoundException("El vendedor indicado no existe"));

        validarVendedor(vendedor.getRol());
        validarMoneda(request.moneda());
        validarSkuEnActualizacion(idProducto, request.sku());
        validarSlugEnActualizacion(idProducto, request.slug());
        validarStock(request.stockFisico(), producto.getStockReservado());

        producto.setCategoria(categoria);
        producto.setVendedor(vendedor);
        producto.setSku(normalizar(request.sku()));
        producto.setSlug(normalizarSlug(request.slug()));
        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
        producto.setMoneda(request.moneda().trim().toUpperCase());
        producto.setPrecioLista(request.precioLista());
        producto.setPrecioVigenteCache(request.precioLista());
        producto.setStockFisico(request.stockFisico());
        producto.setCondicion(request.condicion() == null ? null : request.condicion().trim());
        producto.setEstado(parsearEstadoObligatorio(request.estado()));

        return productoMapper.toResponse(productoRepositorio.save(producto));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public ProductoResponse actualizarPrecio(Long idProducto, ActualizarPrecioProductoRequest request) {
        var producto = productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));

        validarPuedeGestionarProducto(producto);
        validarNuevoPrecio(request.nuevoPrecio());

        var precioAnterior = producto.getPrecioLista();
        if (precioAnterior.compareTo(request.nuevoPrecio()) == 0) {
            return productoMapper.toResponse(producto);
        }

        producto.setPrecioLista(request.nuevoPrecio());
        if (ofertaRepositorio.findOfertaVigente(idProducto, OffsetDateTime.now()).isEmpty()) {
            producto.setPrecioVigenteCache(request.nuevoPrecio());
        }

        registrarHistorialPrecio(producto, precioAnterior, request.nuevoPrecio(), request.motivo());
        return productoMapper.toResponse(productoRepositorio.save(producto));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public ProductoResponse actualizarStock(Long idProducto, ActualizarStockProductoRequest request) {
        var producto = productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));

        validarPuedeGestionarProducto(producto);
        validarStock(request.stockFisico(), producto.getStockReservado());

        var stockFisicoAnterior = producto.getStockFisico();
        if (stockFisicoAnterior.equals(request.stockFisico())) {
            return productoMapper.toResponse(producto);
        }

        producto.setStockFisico(request.stockFisico());
        var guardado = productoRepositorio.save(producto);

        movimientoStockRepositorio.registrarAjusteStock(
                guardado.getId(),
                Math.abs(stockFisicoAnterior - request.stockFisico()),
                stockFisicoAnterior,
                request.stockFisico(),
                guardado.getStockReservado(),
                guardado.getStockReservado(),
                request.motivo() == null || request.motivo().isBlank() ? "Ajuste manual de stock" : request.motivo().trim(),
                OffsetDateTime.now()
        );

        return productoMapper.toResponse(guardado);
    }

    private void validarSku(String sku) {
        if (productoRepositorio.existsBySkuIgnoreCase(sku.trim())) {
            throw new BadRequestException("Ya existe un producto con el SKU indicado");
        }
    }

    private void validarSlug(String slug) {
        if (productoRepositorio.existsBySlugIgnoreCase(slug.trim())) {
            throw new BadRequestException("Ya existe un producto con el slug indicado");
        }
    }

    private void validarMoneda(String moneda) {
        if (!monedaReferenciaRepositorio.existsByCodigoAndActivaTrue(moneda.trim().toUpperCase())) {
            throw new BadRequestException("La moneda indicada no existe o está inactiva");
        }
    }

    private void validarVendedor(RolUsuario rol) {
        if (rol != RolUsuario.VENDEDOR && rol != RolUsuario.ADMIN) {
            throw new BadRequestException("El vendedor indicado debe tener rol VENDEDOR o ADMIN");
        }
    }

    private void validarSkuEnActualizacion(Long idProducto, String sku) {
        if (productoRepositorio.existsBySkuIgnoreCaseAndIdNot(sku.trim(), idProducto)) {
            throw new BadRequestException("Ya existe otro producto con el SKU indicado");
        }
    }

    private void validarSlugEnActualizacion(Long idProducto, String slug) {
        if (productoRepositorio.existsBySlugIgnoreCaseAndIdNot(slug.trim(), idProducto)) {
            throw new BadRequestException("Ya existe otro producto con el slug indicado");
        }
    }

    private void validarStock(Integer stockFisico, Integer stockReservadoActual) {
        if (stockFisico < stockReservadoActual) {
            throw new BadRequestException("El stock físico no puede ser menor al stock reservado actual");
        }
    }

    private void validarNuevoPrecio(BigDecimal nuevoPrecio) {
        if (nuevoPrecio.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El nuevo precio no puede ser menor a 0");
        }
    }

    private void validarPuedeGestionarProducto(ProductoEntidad producto) {
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

    private void validarReasignacionVendedor(Long vendedorIdSolicitado) {
        var usuario = SeguridadUtils.usuarioAutenticado();
        if (usuario.getRol() == RolUsuario.VENDEDOR && !usuario.getId().equals(vendedorIdSolicitado)) {
            throw new ForbiddenException("No tienes permisos para reasignar este producto a otro vendedor");
        }
    }

    private void registrarHistorialPrecio(
            ProductoEntidad producto,
            BigDecimal precioAnterior,
            BigDecimal precioNuevo,
            String motivo
    ) {
        var historial = new ProductoPrecioHistorialEntidad();
        historial.setProducto(producto);
        historial.setMoneda(producto.getMoneda());
        historial.setPrecioAnterior(precioAnterior);
        historial.setPrecioNuevo(precioNuevo);
        historial.setFechaCambio(OffsetDateTime.now());
        historial.setUsuarioCambio(usuarioRepositorioJpa.findById(SeguridadUtils.usuarioId()).orElse(null));
        historial.setMotivo(motivo == null ? null : motivo.trim());
        productoPrecioHistorialRepositorio.save(historial);
    }

    private ProductoDetalleResponse construirDetalleProducto(ProductoEntidad producto) {
        var ofertaVigente = ofertaRepositorio.findOfertaVigente(producto.getId(), OffsetDateTime.now()).orElse(null);
        var resumenResenas = resenaRepositorioJpa.resumirPorProducto(producto.getId());
        return productoMapper.toDetalleResponse(producto, ofertaVigente, resumenResenas);
    }

    private String normalizar(String value) {
        return value.trim();
    }

    private String normalizarSlug(String value) {
        return value.trim().toLowerCase();
    }

    private Pageable crearPageable(FiltroProductoRequest filtro) {
        var pagina = filtro.getPage() == null ? 0 : filtro.getPage();
        var tamano = filtro.getSize() == null ? 10 : filtro.getSize();
        if (pagina < 0) {
            throw new BadRequestException("La página no puede ser negativa");
        }
        if (tamano < 1 || tamano > 100) {
            throw new BadRequestException("El tamaño de página debe estar entre 1 y 100");
        }
        return PageRequest.of(pagina, tamano, construirOrden(filtro.getSort()));
    }

    private Sort construirOrden(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Sort orden = Sort.unsorted();
        for (String criterio : sort) {
            if (criterio == null || criterio.isBlank()) {
                continue;
            }

            var partes = criterio.split(",");
            var propiedad = mapearCampoOrden(partes[0].trim());
            var direccion = partes.length > 1 && "desc".equalsIgnoreCase(partes[1].trim())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            orden = orden.and(Sort.by(direccion, propiedad));
        }

        return orden.isUnsorted() ? Sort.by(Sort.Direction.DESC, "createdAt") : orden;
    }

    private String mapearCampoOrden(String campo) {
        return switch (campo) {
            case "nombre" -> "nombre";
            case "precioLista" -> "precioLista";
            case "createdAt" -> "createdAt";
            case "sku" -> "sku";
            default -> throw new BadRequestException("El campo de ordenamiento no es válido: " + campo);
        };
    }

    private void validarRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new BadRequestException("El precio mínimo no puede ser mayor al precio máximo");
        }
    }

    private EstadoGenerico parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoGenerico.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El estado indicado no es válido");
        }
    }

    private EstadoGenerico parsearEstadoObligatorio(String estado) {
        var estadoParseado = parsearEstado(estado);
        if (estadoParseado == null) {
            throw new BadRequestException("El estado es obligatorio");
        }
        return estadoParseado;
    }
}
