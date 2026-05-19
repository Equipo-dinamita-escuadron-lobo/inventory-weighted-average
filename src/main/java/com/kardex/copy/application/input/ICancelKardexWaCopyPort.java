package com.kardex.copy.application.input;

import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyCancelResponseDto;

/**
 * Puerto de entrada: cancela una copia de kardex.
 * REQ-INVWA-03.
 */
public interface ICancelKardexWaCopyPort {

    CopyCancelResponseDto cancelar(String idProceso);
}
