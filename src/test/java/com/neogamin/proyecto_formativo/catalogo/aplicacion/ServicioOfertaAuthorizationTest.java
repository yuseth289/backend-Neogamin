package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.catalogo.api.dto.CrearOfertaRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.OfertaEntidad;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.OfertaRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorio;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
class ServicioOfertaAuthorizationTest {

    @Mock
    private OfertaRepositorio ofertaRepositorio;
    @Mock
    private ProductoRepositorio productoRepositorio;
    @Mock
    private OfertaMapper ofertaMapper;

    @InjectMocks
    private ServicioOferta servicioOferta;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void vendedorPropietarioPuedeCrearOferta() {
        var vendedor = usuario(2L, RolUsuario.VENDEDOR);
        autenticar(vendedor);
        var producto = producto(10L, vendedor);

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));
        when(ofertaRepositorio.save(any(OfertaEntidad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ofertaMapper.toResponse(any(OfertaEntidad.class))).thenReturn(ofertaResponse(10L));

        assertThatCode(() -> servicioOferta.crearOferta(crearOfertaRequest(10L)))
                .doesNotThrowAnyException();

        verify(ofertaRepositorio).save(any(OfertaEntidad.class));
    }

    @Test
    void vendedorAjenoNoPuedeCrearOferta() {
        autenticar(usuario(3L, RolUsuario.VENDEDOR));
        var producto = producto(10L, usuario(2L, RolUsuario.VENDEDOR));

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> servicioOferta.crearOferta(crearOfertaRequest(10L)))
                .isInstanceOf(ForbiddenException.class);

        verify(ofertaRepositorio, never()).save(any(OfertaEntidad.class));
    }

    @Test
    void adminPuedeCrearOfertaDeCualquierProducto() {
        autenticar(usuario(1L, RolUsuario.ADMIN));
        var producto = producto(10L, usuario(2L, RolUsuario.VENDEDOR));

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));
        when(ofertaRepositorio.save(any(OfertaEntidad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ofertaMapper.toResponse(any(OfertaEntidad.class))).thenReturn(ofertaResponse(10L));

        assertThatCode(() -> servicioOferta.crearOferta(crearOfertaRequest(10L)))
                .doesNotThrowAnyException();

        verify(ofertaRepositorio).save(any(OfertaEntidad.class));
    }

    private CrearOfertaRequest crearOfertaRequest(Long productoId) {
        return new CrearOfertaRequest(
                productoId,
                "Oferta",
                null,
                null,
                new BigDecimal("90000.00"),
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now().plusHours(1),
                "ACTIVO"
        );
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
        producto.setNombre("Producto " + id);
        producto.setPrecioLista(new BigDecimal("100000.00"));
        producto.setPrecioVigenteCache(new BigDecimal("100000.00"));
        return producto;
    }

    private OfertaResponse ofertaResponse(Long productoId) {
        return new OfertaResponse(
                20L,
                productoId,
                "Oferta",
                null,
                null,
                new BigDecimal("90000.00"),
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now().plusHours(1),
                EstadoGenerico.ACTIVO.name()
        );
    }
}
