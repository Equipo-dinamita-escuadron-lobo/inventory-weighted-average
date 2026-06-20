package com.kardex.copy.infrastructure.adapters.input.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de response para cancelar copia de kardex.
 * REQ-INVWA-03.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyCancelResponseDto {

    private String estado;
    private String mensaje;
}
