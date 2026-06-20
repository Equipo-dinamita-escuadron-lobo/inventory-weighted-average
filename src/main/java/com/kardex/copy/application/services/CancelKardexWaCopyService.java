package com.kardex.copy.application.services;

import com.kardex.copy.application.input.ICancelKardexWaCopyPort;
import com.kardex.copy.application.output.ICopyJobLogRepositoryPort;
import com.kardex.copy.domain.enums.CopyEstado;
import com.kardex.copy.domain.exceptions.DuplicateCopyJobException;
import com.kardex.copy.domain.models.CopyJobLog;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyCancelResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Servicio: cancela un proceso de copia de kardex.
 * REQ-INVWA-03.
 */
@Service
@RequiredArgsConstructor
public class CancelKardexWaCopyService implements ICancelKardexWaCopyPort {

    private final ICopyJobLogRepositoryPort logRepo;

    @Override
    public CopyCancelResponseDto cancelar(String idProceso) {
        CopyJobLog log = logRepo.buscarPorIdProceso(idProceso)
                .orElseThrow(() -> new DuplicateCopyJobException(idProceso, 0));

        logRepo.guardar(CopyJobLog.builder()
                .idProceso(log.getIdProceso())
                .fase(log.getFase())
                .modulo(log.getModulo())
                .estado(CopyEstado.CANCELADO)
                .fechaInicio(log.getFechaInicio())
                .fechaFin(Instant.now())
                .equivalenciasGeneradas(log.getEquivalenciasGeneradas())
                .build());

        return CopyCancelResponseDto.builder()
                .estado(CopyEstado.CANCELADO.name())
                .mensaje("Proceso de copia kardex weighted-average cancelado exitosamente")
                .build();
    }
}
