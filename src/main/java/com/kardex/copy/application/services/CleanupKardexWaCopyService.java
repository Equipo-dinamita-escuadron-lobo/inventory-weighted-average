package com.kardex.copy.application.services;

import com.kardex.copy.application.input.ICleanupKardexWaCopyPort;
import com.kardex.copy.application.output.ICopyJobLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio: limpia registros de copia de kardex.
 * REQ-INVWA-03.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupKardexWaCopyService implements ICleanupKardexWaCopyPort {

    private final ICopyJobLogRepositoryPort logRepo;

    @Override
    public void limpiar(String idProceso) {
        log.info("Limpiando registros de copia kardex weighted-average para proceso {}", idProceso);
        logRepo.eliminarPorIdProceso(idProceso);
    }
}
