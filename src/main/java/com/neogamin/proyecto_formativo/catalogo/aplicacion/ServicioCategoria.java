package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.CategoriaArbolResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CategoriaResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CrearCategoriaRequest;
import com.neogamin.proyecto_formativo.catalogo.dominio.Categoria;
import com.neogamin.proyecto_formativo.catalogo.api.dto.FiltroProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoListadoResponse;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.CategoriaRepositorioJpa;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioCategoria {

    private final CategoriaRepositorioJpa categoriaRepositorioJpa;
    private final CategoriaMapper categoriaMapper;
    private final ServicioProducto servicioProducto;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaResponse crearCategoria(CrearCategoriaRequest request) {
        String slugNormalizado = normalizarSlug(request.slug());
        validarSlug(slugNormalizado);

        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre().trim());
        categoria.setSlug(slugNormalizado);
        categoria.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : null);
        categoria.setEstado(EstadoGenerico.ACTIVO);

        if (request.categoriaPadreId() != null) {
            Categoria categoriaPadre = categoriaRepositorioJpa.findByIdAndDeletedAtIsNull(request.categoriaPadreId())
                    .orElseThrow(() -> new NotFoundException("La categoria padre indicada no existe"));
            categoria.setCategoriaPadre(categoriaPadre);
        }

        return categoriaMapper.toResponse(categoriaRepositorioJpa.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaArbolResponse> listarArbol() {
        var categorias = categoriaRepositorioJpa.findActivasNoEliminadasOrderByNombreAsc();

        Map<Long, CategoriaArbolResponse> categoriasPorId = new LinkedHashMap<>();
        for (var categoria : categorias) {
            categoriasPorId.put(categoria.getId(), categoriaMapper.toArbolResponse(categoria));
        }

        List<CategoriaArbolResponse> raices = new ArrayList<>();
        for (var categoria : categorias) {
            var nodoActual = categoriasPorId.get(categoria.getId());
            var categoriaPadre = categoria.getCategoriaPadre();

            if (categoriaPadre == null) {
                raices.add(nodoActual);
                continue;
            }

            var nodoPadre = categoriasPorId.get(categoriaPadre.getId());
            if (nodoPadre == null) {
                raices.add(nodoActual);
                continue;
            }

            nodoPadre.getSubcategorias().add(nodoActual);
        }

        return raices;
    }

    @Transactional(readOnly = true)
    public Page<ProductoListadoResponse> listarProductosPorCategoria(Long idCategoria, FiltroProductoRequest filtro) {
        categoriaRepositorioJpa.findByIdAndDeletedAtIsNull(idCategoria)
                .orElseThrow(() -> new NotFoundException("La categoría indicada no existe"));

        filtro.setIdCategoria(idCategoria);
        return servicioProducto.listarProductos(filtro);
    }

    private void validarSlug(String slug) {
        if (categoriaRepositorioJpa.existsBySlugIgnoreCase(slug)) {
            throw new BadRequestException("Ya existe una categoria con el slug indicado");
        }
    }

    private String normalizarSlug(String slug) {
        String normalizado = slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT);
        if (normalizado.isBlank()) {
            throw new BadRequestException("El slug de la categoria es obligatorio");
        }
        return normalizado;
    }
}
