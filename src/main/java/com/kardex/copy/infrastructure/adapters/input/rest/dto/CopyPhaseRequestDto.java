package com.kardex.copy.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request para ejecutar una fase de copia de kardex weighted-average.
 * equivalenciasPrev contiene remap idProduct → nuevo idProduct en destino (tabla "product").
 * Contrato uniforme REQ-INVWA-02, ADR-43 (KARDEX → {PRODUCTS}).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyPhaseRequestDto {

    @NotNull
    private UUID idProceso;

    @Positive
    private int fase;

    @NotBlank
    private String entOrigen;

    @NotBlank
    private String entDestino;

    @NotNull
    private Instant snapshotCorte;

    /**
     * Equivalencias previas para remapear idProduct del ProductEntity local.
     * Tabla esperada: "product".
     * REQ-INVWA-02.
     */
    private List<CopyEquivalenciaDto> equivalenciasPrev;
}
