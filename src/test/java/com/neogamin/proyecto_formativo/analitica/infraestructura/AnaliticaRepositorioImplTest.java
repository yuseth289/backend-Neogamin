package com.neogamin.proyecto_formativo.analitica.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnaliticaRepositorioImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private AnaliticaRepositorioImpl repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new AnaliticaRepositorioImpl();
        ReflectionTestUtils.setField(repositorio, "entityManager", entityManager);
    }

    @Test
    void resumenVendedorUsaEstadosRealesDelEnumYMapeaTotales() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter("vendedorId", 2L)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[]{
                new BigDecimal("300000.00"),
                new BigDecimal("100000.00"),
                2L,
                new BigDecimal("150000.00")
        });

        var response = repositorio.obtenerResumenVendedor(2L);

        assertThat(response.ingresosTotales()).isEqualByComparingTo("300000.00");
        assertThat(response.ingresosMesActual()).isEqualByComparingTo("100000.00");
        assertThat(response.cantidadPedidosVendidos()).isEqualTo(2L);
        assertThat(response.ticketPromedio()).isEqualByComparingTo("150000.00");

        var sql = capturarSql();
        assertThat(sql).contains("p.estado::text in ('PAGADO', 'PREPARANDO', 'ENVIADO', 'ENTREGADO')");
    }

    @Test
    void metodosPagoUsaEstadosMayusculaDePedidoYPagoYMapeaResultados() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Object[]{
                "TARJETA",
                2L,
                new BigDecimal("300000.00")
        }));

        var response = repositorio.obtenerMetodosPagoMasUsados();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).metodoPago()).isEqualTo("TARJETA");
        assertThat(response.get(0).cantidadUsos()).isEqualTo(2L);
        assertThat(response.get(0).montoTotal()).isEqualByComparingTo("300000.00");

        var sql = capturarSql();
        assertThat(sql).contains("p.estado::text in ('PAGADO', 'PREPARANDO', 'ENVIADO', 'ENTREGADO')");
        assertThat(sql).contains("pa.estado::text not in ('RECHAZADO', 'ANULADO')");
    }

    private String capturarSql() {
        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture());
        return sqlCaptor.getValue();
    }
}
