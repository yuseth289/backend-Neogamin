package com.neogamin.proyecto_formativo.pago.infraestructura;

import com.neogamin.proyecto_formativo.pago.dominio.Pago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepositorioJpa extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);
}
