package com.kardex.copy.application;

import com.kardex.copy.application.input.IExecuteKardexWaCopyPhasePort;
import com.kardex.copy.application.output.*;
import com.kardex.copy.application.services.CopyKardexWaService;
import com.kardex.copy.domain.enums.CopyEstado;
import com.kardex.copy.domain.models.CopyJobLog;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyEquivalenciaDto;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyPhaseResponseDto;
import com.kardex.domain.model.MovementType;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TDD RED → GREEN: tests unitarios para CopyKardexWaService.
 * Kardex WA copia: ProductEntity (con remap idProduct) → KardexEntity (asociado a nuevo Product).
 * REQ-INVWA-01, REQ-INVWA-02, ADR-38, ADR-43.
 */
@ExtendWith(MockitoExtension.class)
class CopyKardexWaServiceTest {

    @Mock
    private ICopyJobLogRepositoryPort logRepo;
    @Mock
    private IProductCopySourceRepositoryPort productSourceRepo;
    @Mock
    private IProductCopyTargetRepositoryPort productTargetRepo;
    @Mock
    private IKardexCopySourceRepositoryPort kardexSourceRepo;
    @Mock
    private IKardexCopyTargetRepositoryPort kardexTargetRepo;

    private IExecuteKardexWaCopyPhasePort service;

    @BeforeEach
    void setUp() {
        service = new CopyKardexWaService(logRepo, productSourceRepo, productTargetRepo,
                kardexSourceRepo, kardexTargetRepo);
    }

    @Test
    @DisplayName("copia exitosa: 1 producto + 2 movimientos kardex con remap idProduct")
    void ejecutar_copiaProductoYKardex_completado() {
        UUID idProceso = UUID.randomUUID();
        String origen = "empresa-A";
        String destino = "empresa-B";
        Instant snapshot = Instant.now();

        // Equivalencia: idProduct externo 100 (origen) → 200 (destino)
        CopyEquivalenciaDto equivProduct = CopyEquivalenciaDto.builder()
                .modulo("products")
                .tabla("product")
                .idViejo("100")
                .idNuevo("200")
                .build();

        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(3)
                .entOrigen(origen)
                .entDestino(destino)
                .snapshotCorte(snapshot)
                .equivalenciasPrev(List.of(equivProduct))
                .build();

        // Producto origen
        ProductEntity prodOrigen = new ProductEntity();
        prodOrigen.setId(1L);
        prodOrigen.setProductId(100L);
        prodOrigen.setName("Producto X");
        prodOrigen.setReference("REF-001");
        prodOrigen.setEnterpriseId(origen);

        // Producto destino (guardado)
        ProductEntity prodDestino = new ProductEntity();
        prodDestino.setId(50L);
        prodDestino.setProductId(200L); // remapeado
        prodDestino.setName("Producto X");
        prodDestino.setReference("REF-001");
        prodDestino.setEnterpriseId(destino);

        // Kardex entries
        KardexEntity k1 = new KardexEntity();
        k1.setId(10L);
        k1.setQuantity(50);
        k1.setUnitPrice(BigDecimal.valueOf(100));
        k1.setDetails("Compra");
        k1.setType(MovementType.PURCHASE);
        k1.setBalanceQuantity(50);
        k1.setBalanceUnitPrice(BigDecimal.valueOf(100));
        k1.setDate(ZonedDateTime.now());

        KardexEntity k2 = new KardexEntity();
        k2.setId(11L);
        k2.setQuantity(10);
        k2.setUnitPrice(BigDecimal.valueOf(100));
        k2.setDetails("Venta");
        k2.setType(MovementType.SALE);
        k2.setBalanceQuantity(40);
        k2.setBalanceUnitPrice(BigDecimal.valueOf(100));
        k2.setDate(ZonedDateTime.now());

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 3)).thenReturn(Optional.empty());
        when(productSourceRepo.findByEnterpriseId(origen)).thenReturn(List.of(prodOrigen));
        when(productTargetRepo.guardar(any(ProductEntity.class))).thenReturn(prodDestino);
        when(kardexSourceRepo.findByProductId(1L)).thenReturn(List.of(k1, k2));
        when(kardexTargetRepo.guardar(any(KardexEntity.class))).thenAnswer(inv -> {
            KardexEntity ke = inv.getArgument(0);
            ke.setId(100L);
            return ke;
        });
        when(logRepo.guardar(any(CopyJobLog.class))).thenAnswer(inv -> inv.getArgument(0));

        CopyPhaseResponseDto response = service.ejecutar(request);

        assertThat(response.getEstado()).isEqualTo(CopyEstado.COMPLETADO.name());
        assertThat(response.getRegistrosProcesados()).isEqualTo(3); // 1 product + 2 kardex
        // Verificar que idProduct fue remapeado a 200 en el producto guardado
        verify(productTargetRepo).guardar(argThat(p -> Long.valueOf(200L).equals(p.getProductId())));
        // Verificar que los 2 movimientos fueron copiados con el nuevo ProductEntity
        verify(kardexTargetRepo, times(2)).guardar(any());
    }

    @Test
    @DisplayName("idProduct sin equivalencia: genera advertencia, producto se guarda con idProduct null")
    void ejecutar_idProductSinEquivalencia_advertencia() {
        UUID idProceso = UUID.randomUUID();

        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(3)
                .entOrigen("A")
                .entDestino("B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(Collections.emptyList()) // sin equivalencias
                .build();

        ProductEntity prodOrigen = new ProductEntity();
        prodOrigen.setId(1L);
        prodOrigen.setProductId(100L);
        prodOrigen.setName("P");
        prodOrigen.setReference("R");
        prodOrigen.setEnterpriseId("A");

        ProductEntity prodGuardado = new ProductEntity();
        prodGuardado.setId(50L);
        prodGuardado.setProductId(null); // sin equivalencia → null
        prodGuardado.setName("P");
        prodGuardado.setReference("R");
        prodGuardado.setEnterpriseId("B");

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 3)).thenReturn(Optional.empty());
        when(productSourceRepo.findByEnterpriseId("A")).thenReturn(List.of(prodOrigen));
        when(productTargetRepo.guardar(any())).thenReturn(prodGuardado);
        when(kardexSourceRepo.findByProductId(1L)).thenReturn(Collections.emptyList());
        when(logRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        CopyPhaseResponseDto response = service.ejecutar(request);

        assertThat(response.getEstado()).isEqualTo(CopyEstado.COMPLETADO_CON_ADVERTENCIAS.name());
        assertThat(response.getAdvertencias()).isNotEmpty();
    }

    @Test
    @DisplayName("idempotencia: retorna resultado previo si la fase ya fue ejecutada")
    void ejecutar_idempotencia_retornaResultadoPrevio() {
        UUID idProceso = UUID.randomUUID();

        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(3)
                .entOrigen("A")
                .entDestino("B")
                .snapshotCorte(Instant.now())
                .build();

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 3)).thenReturn(
                Optional.of(CopyJobLog.builder().estado(CopyEstado.COMPLETADO).equivalenciasGeneradas(5).build()));

        CopyPhaseResponseDto response = service.ejecutar(request);

        assertThat(response.getMensaje()).contains("idempotencia");
        verify(productSourceRepo, never()).findByEnterpriseId(any());
    }

    @Test
    @DisplayName("error: origen igual a destino retorna ERROR_NO_REINTENTABLE")
    void ejecutar_origenIgualDestino_errorNoReintentable() {
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(UUID.randomUUID())
                .fase(3)
                .entOrigen("A")
                .entDestino("A")
                .snapshotCorte(Instant.now())
                .build();

        assertThat(service.ejecutar(request).getEstado()).isEqualTo("ERROR_NO_REINTENTABLE");
    }
}
