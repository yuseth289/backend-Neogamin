package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.catalogo.api.dto.AgregarProductoImagenRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoImagenResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoImagenEntidad;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoImagenRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorio;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
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
class ServicioProductoImagenAuthorizationTest {

    @Mock
    private ProductoRepositorio productoRepositorio;
    @Mock
    private ProductoImagenRepositorio productoImagenRepositorio;
    @Mock
    private ProductoImagenMapper productoImagenMapper;

    @InjectMocks
    private ServicioProductoImagen servicioProductoImagen;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void vendedorPropietarioPuedeAgregarImagen() {
        var vendedor = usuario(2L, RolUsuario.VENDEDOR);
        autenticar(vendedor);
        var producto = producto(10L, vendedor);

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));
        when(productoImagenRepositorio.save(any(ProductoImagenEntidad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productoImagenMapper.toResponse(any(ProductoImagenEntidad.class))).thenReturn(imagenResponse(10L));

        assertThatCode(() -> servicioProductoImagen.agregarImagen(10L, agregarImagenRequest()))
                .doesNotThrowAnyException();

        verify(productoImagenRepositorio).save(any(ProductoImagenEntidad.class));
    }

    @Test
    void vendedorAjenoNoPuedeAgregarImagen() {
        autenticar(usuario(3L, RolUsuario.VENDEDOR));
        var producto = producto(10L, usuario(2L, RolUsuario.VENDEDOR));

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> servicioProductoImagen.agregarImagen(10L, agregarImagenRequest()))
                .isInstanceOf(ForbiddenException.class);

        verify(productoImagenRepositorio, never()).save(any(ProductoImagenEntidad.class));
    }

    @Test
    void adminPuedeCambiarImagenPrincipalDeCualquierProducto() {
        autenticar(usuario(1L, RolUsuario.ADMIN));
        var producto = producto(10L, usuario(2L, RolUsuario.VENDEDOR));
        var imagen = imagen(30L, producto);

        when(productoRepositorio.findById(10L)).thenReturn(Optional.of(producto));
        when(productoImagenRepositorio.findByIdAndProductoIdAndDeletedAtIsNull(30L, 10L)).thenReturn(Optional.of(imagen));
        when(productoImagenRepositorio.findByProductoIdAndPrincipalTrueAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());
        when(productoImagenRepositorio.save(imagen)).thenReturn(imagen);
        when(productoImagenMapper.toResponse(imagen)).thenReturn(imagenResponse(10L));

        assertThatCode(() -> servicioProductoImagen.cambiarImagenPrincipal(10L, 30L))
                .doesNotThrowAnyException();

        verify(productoImagenRepositorio).save(imagen);
    }

    private AgregarProductoImagenRequest agregarImagenRequest() {
        return new AgregarProductoImagenRequest(
                "https://example.com/producto.png",
                "Producto",
                1,
                false
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
        return producto;
    }

    private ProductoImagenEntidad imagen(Long id, ProductoEntidad producto) {
        var imagen = new ProductoImagenEntidad();
        imagen.setId(id);
        imagen.setProducto(producto);
        imagen.setPrincipal(false);
        return imagen;
    }

    private ProductoImagenResponse imagenResponse(Long productoId) {
        return new ProductoImagenResponse(30L, productoId, "https://example.com/producto.png", "Producto", 1, true);
    }
}
