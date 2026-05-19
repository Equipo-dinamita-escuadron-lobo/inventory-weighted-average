package com.kardex.copy.application.services;

import com.kardex.copy.application.input.IGetKardexWaCopyStatusPort;
import com.kardex.copy.application.output.ICopyJobLogRepositoryPort;
import com.kardex.copy.domain.exceptions.DuplicateCopyJobException;
import com.kardex.copy.domain.models.CopyJobLog;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyStatusResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio: consulta el estado de un proceso de copia de kardex.
 * REQ-INVWA-03.
 */
@Service
@RequiredArgsConstructor
public class GetKardexWaCopyStatusService implements IGetKardexWaCopyStatusPort {

    private final ICopyJobLogRepositoryPort logRepo;

    @Override
    public CopyStatusResponseDto obtenerEstado(String idProceso) {
        CopyJobLog log = logRepo.buscarPorIdProceso(idProceso)
                .orElseThrow(() -> new DuplicateCopyJobException(idProceso, 0));

        return CopyStatusResponseDto.builder()
                .fase(log.getFase() != null ? log.getFase() : 0)
                .estado(log.getEstado() != null ? log.getEstado().name() : "DESCONOCIDO")
                .registrosProcesados(log.getEquivalenciasGeneradas() != null ? log.getEquivalenciasGeneradas() : 0)
                .intentos(1)
                .ultimoError(log.getErrorMessage())
                .build();
    }
}
