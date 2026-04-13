package com.neogamin.proyecto_formativo.usuario.infraestructura;

import com.neogamin.proyecto_formativo.usuario.dominio.DatosPagoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatosPagoRepositorio extends JpaRepository<DatosPagoEntidad, Long> {
}
