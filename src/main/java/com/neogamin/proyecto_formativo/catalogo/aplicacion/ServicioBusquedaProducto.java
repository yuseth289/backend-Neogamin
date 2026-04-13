package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.BusquedaNaturalProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoBusquedaResponse;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.BusquedaProductoRepositorio;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ConsultaBusquedaProducto;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioBusquedaProducto {

    private final BusquedaProductoRepositorio busquedaProductoRepositorio;
    private final BusquedaProductoMapper busquedaProductoMapper;
    private final InterpretadorConsultaProducto interpretadorConsultaProducto;

    @Transactional(readOnly = true)
    public Page<ProductoBusquedaResponse> buscar(BusquedaNaturalProductoRequest request) {
        validarPaginacion(request);
        var consulta = construirConsulta(request);
        return busquedaProductoRepositorio.buscar(consulta).map(busquedaProductoMapper::toResponse);
    }

    private void validarPaginacion(BusquedaNaturalProductoRequest request) {
        var pagina = request.getPage() == null ? 0 : request.getPage();
        var tamano = request.getSize() == null ? 10 : request.getSize();
        if (pagina < 0) {
            throw new BadRequestException("La página no puede ser negativa");
        }
        if (tamano < 1 || tamano > 100) {
            throw new BadRequestException("El tamaño de página debe estar entre 1 y 100");
        }
    }

    private ConsultaBusquedaProducto construirConsulta(BusquedaNaturalProductoRequest request) {
        var interpretada = interpretadorConsultaProducto.interpretar(request.getTexto());
        if (interpretada.textoNormalizado().isBlank()) {
            throw new BadRequestException("El texto de búsqueda es obligatorio");
        }

        return new ConsultaBusquedaProducto(
                interpretada.textoOriginal(),
                interpretada.textoNormalizado(),
                interpretada.textoConsultaFts(),
                interpretada.terminos(),
                interpretada.aliasTipoProducto(),
                interpretada.buscarSoloConOferta(),
                Boolean.TRUE.equals(request.getSoloDisponibles()),
                request.getPage() == null ? 0 : request.getPage(),
                request.getSize() == null ? 10 : request.getSize(),
                interpretada.intencionPrecio()
        );
    }
}
