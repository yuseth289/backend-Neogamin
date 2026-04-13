package com.neogamin.proyecto_formativo.catalogo.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoriaArbolResponse {

    private Long idCategoria;
    private String nombre;
    private String slug;
    private String descripcion;
    private List<CategoriaArbolResponse> subcategorias = new ArrayList<>();

    public CategoriaArbolResponse(Long idCategoria, String nombre, String slug, String descripcion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.slug = slug;
        this.descripcion = descripcion;
    }
}
