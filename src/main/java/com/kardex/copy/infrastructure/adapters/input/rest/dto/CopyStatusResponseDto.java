package com.kardex.copy.infrastructure.adapters.input.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de response para consultar estado de copia de kardex.
 * REQ-INVWA-03.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyStatusResponseDto {

    private Integer fase;
    private String estado;
    private Integer registrosProcesados;
    private Integer intentos;
    private String ultimoError;
}
