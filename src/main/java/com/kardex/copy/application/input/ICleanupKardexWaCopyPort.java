package com.kardex.copy.application.input;

/**
 * Puerto de entrada: limpia registros de copia de kardex.
 * REQ-INVWA-03.
 */
public interface ICleanupKardexWaCopyPort {

    void limpiar(String idProceso);
}
