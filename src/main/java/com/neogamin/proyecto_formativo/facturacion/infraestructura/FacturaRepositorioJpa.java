package com.neogamin.proyecto_formativo.facturacion.infraestructura;

import com.neogamin.proyecto_formativo.facturacion.dominio.Factura;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaRepositorioJpa extends JpaRepository<Factura, Long> {

    Optional<Factura> findByPedidoId(Long pedidoId);
}
