package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.CategoriaArbolResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CategoriaResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public CategoriaArbolResponse toArbolResponse(Categoria categoria) {
        return new CategoriaArbolResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getSlug(),
                categoria.getDescripcion()
        );
    }

    public CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getCategoriaPadre() != null ? categoria.getCategoriaPadre().getId() : null,
                categoria.getNombre(),
                categoria.getSlug(),
                categoria.getDescripcion(),
                categoria.getEstado().name()
        );
    }
}
