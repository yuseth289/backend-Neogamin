package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.catalogo.api.dto.ActualizarPrecioProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ActualizarStockProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.CategoriaRepositorioJpa;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.MonedaReferenciaRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.MovimientoStockRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.OfertaRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoPrecioHistorialRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorio;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.resena.infraestructura.ResenaRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ServicioProductoAuthorizationTest {

    @Mock
    private ProductoRepositorio productoRepositorio;
    @Mock
    private CategoriaRepositorioJpa categoriaRepositorioJpa;
    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;
    @Mock
    private MonedaReferenciaRepositorio monedaReferenciaRepositorio;
    @Mock
    private MovimientoStockRepositorio movimientoStockRepositorio;
    @Mock
    private OfertaRepositorio ofertaRepositorio;
    @Mock
    private ProductoPrecioHistorialRepositorio productoPrecioHistorialRepositorio;
    @Mock
    private ResenaRepositorioJpa resenaRepositorioJpa;
    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ServicioProducto servicioProducto;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void vendedorPropietarioPuedeActualizarPrecio() {
        var vendedor = usuario(2L, RolUsuario.VENDEDOR);
        autenticar(vendedor);
        var producto = producto(10L, vendedor);

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));
        when(ofertaRepositorio.findOfertaVigente(any(), any())).thenReturn(Optional.empty());
        when(usuarioRepositorioJpa.findById(2L)).thenReturn(Optional.of(vendedor));
        when(productoRepositorio.save(producto)).thenReturn(producto);
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse(producto));

        assertThatCode(() -> servicioProducto.actualizarPrecio(
                10L,
                new ActualizarPrecioProductoRequest(new BigDecimal("120000.00"), "Ajuste")
        )).doesNotThrowAnyException();

        verify(productoRepositorio).save(producto);
    }

    @Test
    void vendedorAjenoNoPuedeActualizarStock() {
        autenticar(usuario(3L, RolUsuario.VENDEDOR));
        var producto = producto(10L, usuario(2L, RolUsuario.VENDEDOR));

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> servicioProducto.actualizarStock(
                10L,
                new ActualizarStockProductoRequest(20, "Ajuste")
        )).isInstanceOf(ForbiddenException.class);

        verify(productoRepositorio, never()).save(any(ProductoEntidad.class));
        verify(movimientoStockRepositorio, never()).registrarAjusteStock(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void adminPuedeActualizarPrecioDeCualquierProducto() {
        var admin = usuario(1L, RolUsuario.ADMIN);
        autenticar(admin);
        var producto = producto(10L, usuario(2L, RolUsuario.VENDEDOR));

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));
        when(ofertaRepositorio.findOfertaVigente(any(), any())).thenReturn(Optional.empty());
        when(usuarioRepositorioJpa.findById(1L)).thenReturn(Optional.of(admin));
        when(productoRepositorio.save(producto)).thenReturn(producto);
        when(productoMapper.toResponse(producto)).thenReturn(productoResponse(producto));

        assertThatCode(() -> servicioProducto.actualizarPrecio(
                10L,
                new ActualizarPrecioProductoRequest(new BigDecimal("120000.00"), "Ajuste admin")
        )).doesNotThrowAnyException();

        verify(productoRepositorio).save(producto);
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities())
        );
    }

    private Usuario usuario(Long id, RolUsuario rol) {
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Usuario " + id);
        usuario.setEmail("usuario" + id + "@example.com");
        usuario.setPasswordHash("hash");
        usuario.setRol(rol);
        usuario.setEstado(EstadoGenerico.ACTIVO);
        return usuario;
    }

    private ProductoEntidad producto(Long id, Usuario vendedor) {
        var producto = new ProductoEntidad();
        producto.setId(id);
        producto.setVendedor(vendedor);
        producto.setSku("SKU-" + id);
        producto.setSlug("producto-" + id);
        producto.setNombre("Producto " + id);
        producto.setMoneda("COP");
        producto.setPrecioLista(new BigDecimal("100000.00"));
        producto.setPrecioVigenteCache(new BigDecimal("100000.00"));
        producto.setStockFisico(10);
        producto.setStockReservado(0);
        producto.setEstado(EstadoGenerico.ACTIVO);
        return producto;
    }

    private ProductoResponse productoResponse(ProductoEntidad producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getSku(),
                producto.getSlug(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getMoneda(),
                producto.getPrecioLista(),
                producto.getPrecioVigenteCache(),
                producto.getStockFisico(),
                producto.getStockReservado(),
                producto.getCondicion(),
                producto.getEstado().name(),
                1L,
                producto.getVendedor().getId()
        );
    }
}
