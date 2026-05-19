package com.kardex.copy.application.input;

import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyPhaseResponseDto;

/**
 * Puerto de entrada: ejecuta una fase de copia de kardex weighted-average.
 * REQ-INVWA-01, ADR-38.
 */
public interface IExecuteKardexWaCopyPhasePort {

    CopyPhaseResponseDto ejecutar(CopyPhaseRequestDto request);
}
