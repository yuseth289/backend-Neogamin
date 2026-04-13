package com.neogamin.proyecto_formativo.catalogo.api;

import com.neogamin.proyecto_formativo.catalogo.api.dto.AgregarProductoImagenRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ActualizarPrecioProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ActualizarStockProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ActualizarProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CrearProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.FiltroProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoDetalleResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoImagenResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoListadoResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoResponse;
import com.neogamin.proyecto_formativo.catalogo.aplicacion.ServicioProductoImagen;
import com.neogamin.proyecto_formativo.catalogo.aplicacion.ServicioProducto;
import com.neogamin.proyecto_formativo.compartido.infraestructura.OpenApiConfiguracion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo/productos")
@RequiredArgsConstructor
@Tag(name = "Catalogo - Productos", description = "Consulta y administracion de productos del catalogo.")
public class ControladorProducto {

    private final ServicioProducto servicioProducto;
    private final ServicioProductoImagen servicioProductoImagen;

    @GetMapping
    @Operation(
            summary = "Listar productos",
            description = "Lista productos del catalogo con filtros opcionales, paginacion y ordenamiento."
    )
    @ApiResponse(responseCode = "200", description = "Listado generado correctamente")
    public ResponseEntity<Page<ProductoListadoResponse>> listar(
            @Valid @ModelAttribute FiltroProductoRequest filtro
    ) {
        return ResponseEntity.ok(servicioProducto.listarProductos(filtro));
    }

    @GetMapping("/{idProducto}")
    @Operation(summary = "Ver detalle de producto por id")
    public ResponseEntity<ProductoDetalleResponse> obtenerDetallePorId(@PathVariable Long idProducto) {
        return ResponseEntity.ok(servicioProducto.obtenerDetallePorId(idProducto));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Ver detalle de producto por slug")
    public ResponseEntity<ProductoDetalleResponse> obtenerDetallePorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(servicioProducto.obtenerDetallePorSlug(slug));
    }

    @PostMapping
    @Operation(
            summary = "Crear producto",
            description = "Crea un nuevo producto del catalogo. Requiere un JWT valido con rol ADMIN o VENDEDOR."
    )
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductoResponse.class),
                            examples = @ExampleObject(
                                    name = "Producto creado",
                                    value = """
                                            {
                                              "id": 11,
                                              "sku": "SKU-SM-200",
                                              "slug": "xiaomi-redmi-note-13-pro",
                                              "nombre": "Xiaomi Redmi Note 13 Pro",
                                              "descripcion": "Smartphone 256GB, 8GB RAM, pantalla AMOLED 120Hz",
                                              "moneda": "COP",
                                              "precioLista": 1599000.00,
                                              "precioVigente": 1599000.00,
                                              "stockFisico": 25,
                                              "stockReservado": 0,
                                              "condicion": "nuevo",
                                              "estado": "ACTIVO",
                                              "categoriaId": 4,
                                              "vendedorId": 2
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Validacion o regla de negocio incumplida"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para crear productos")
    })
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody CrearProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioProducto.crearProducto(request));
    }

    @PutMapping("/{idProducto}")
    @Operation(summary = "Actualizar producto")
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    public ResponseEntity<ProductoResponse> actualizar(
            @Parameter(description = "Identificador del producto", example = "11")
            @PathVariable Long idProducto,
            @Valid @RequestBody ActualizarProductoRequest request
    ) {
        return ResponseEntity.ok(servicioProducto.actualizarProducto(idProducto, request));
    }

    @PatchMapping("/{idProducto}/precio")
    @Operation(summary = "Actualizar precio de producto")
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    public ResponseEntity<ProductoResponse> actualizarPrecio(
            @PathVariable Long idProducto,
            @Valid @RequestBody ActualizarPrecioProductoRequest request
    ) {
        return ResponseEntity.ok(servicioProducto.actualizarPrecio(idProducto, request));
    }

    @PatchMapping("/{idProducto}/stock")
    @Operation(summary = "Actualizar stock del producto")
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    public ResponseEntity<ProductoResponse> actualizarStock(
            @PathVariable Long idProducto,
            @Valid @RequestBody ActualizarStockProductoRequest request
    ) {
        return ResponseEntity.ok(servicioProducto.actualizarStock(idProducto, request));
    }

    @PostMapping("/{idProducto}/imagenes")
    @Operation(summary = "Agregar imagen a un producto")
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    public ResponseEntity<ProductoImagenResponse> agregarImagen(
            @PathVariable Long idProducto,
            @Valid @RequestBody AgregarProductoImagenRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioProductoImagen.agregarImagen(idProducto, request));
    }

    @PatchMapping("/{idProducto}/imagenes/{idImagen}/principal")
    @Operation(summary = "Cambiar imagen principal de un producto")
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    public ResponseEntity<ProductoImagenResponse> cambiarImagenPrincipal(
            @PathVariable Long idProducto,
            @PathVariable Long idImagen
    ) {
        return ResponseEntity.ok(servicioProductoImagen.cambiarImagenPrincipal(idProducto, idImagen));
    }
}
