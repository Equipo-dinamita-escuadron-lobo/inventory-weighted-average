package com.kardex.copy.domain.exceptions;

/**
 * Excepción: proceso de copia no encontrado.
 * REQ-INVWA-01.
 */
public class DuplicateCopyJobException extends RuntimeException {

    public DuplicateCopyJobException(String idProceso, int fase) {
        super("No se encontró proceso de copia: idProceso=" + idProceso + ", fase=" + fase);
    }
}
