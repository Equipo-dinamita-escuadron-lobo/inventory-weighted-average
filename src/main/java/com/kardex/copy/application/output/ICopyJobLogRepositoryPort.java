package com.kardex.copy.application.output;

import com.kardex.copy.domain.models.CopyJobLog;

import java.util.Optional;

/**
 * Puerto de salida: log de idempotencia de copia de kardex weighted-average.
 * REQ-INVWA-01.
 */
public interface ICopyJobLogRepositoryPort {

    CopyJobLog guardar(CopyJobLog log);

    Optional<CopyJobLog> buscarPorIdProcesoYFase(String idProceso, int fase);

    Optional<CopyJobLog> buscarPorIdProceso(String idProceso);

    void eliminarPorIdProceso(String idProceso);
}
