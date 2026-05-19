package com.kardex.copy.application.input;

import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyStatusResponseDto;

/**
 * Puerto de entrada: consulta estado de copia de kardex.
 * REQ-INVWA-03.
 */
public interface IGetKardexWaCopyStatusPort {

    CopyStatusResponseDto obtenerEstado(String idProceso);
}
