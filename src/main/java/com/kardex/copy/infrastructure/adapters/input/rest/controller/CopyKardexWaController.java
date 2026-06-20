package com.kardex.copy.infrastructure.adapters.input.rest.controller;

import com.kardex.copy.application.input.*;
import com.kardex.copy.domain.exceptions.DuplicateCopyJobException;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST del bounded context copy en inventory-weighted-average.
 * Expone los 4 endpoints del contrato uniforme bajo /api/kardex/weighted-average/copy.
 * REQ-INVWA-03, ADR-38.
 */
@RestController
@RequestMapping("/api/kardex/weighted-average/copy")
@RequiredArgsConstructor
@Slf4j
public class CopyKardexWaController {

    private final IExecuteKardexWaCopyPhasePort executePort;
    private final IGetKardexWaCopyStatusPort statusPort;
    private final ICancelKardexWaCopyPort cancelPort;
    private final ICleanupKardexWaCopyPort cleanupPort;

    /**
     * POST /api/kardex/weighted-average/copy/phase
     * Copia ProductEntity + KardexEntity del tenant origen al destino.
     * equivalenciasPrev debe incluir remap tabla "product" (idProduct externo).
     */
    @PostMapping("/phase")
    public ResponseEntity<CopyPhaseResponseDto> executePhase(
            @Valid @RequestBody CopyPhaseRequestDto request) {
        log.info("Ejecutando fase {} para proceso {} en kardex weighted-average",
                request.getFase(), request.getIdProceso());
        CopyPhaseResponseDto response = executePort.ejecutar(request);
        return ResponseEntity.status(resolverHttpStatus(response.getEstado())).body(response);
    }

    /** GET /api/kardex/weighted-average/copy/{idProceso}/status */
    @GetMapping("/{idProceso}/status")
    public ResponseEntity<CopyStatusResponseDto> getStatus(@PathVariable String idProceso) {
        return ResponseEntity.ok(statusPort.obtenerEstado(idProceso));
    }

    /** POST /api/kardex/weighted-average/copy/{idProceso}/cancel */
    @PostMapping("/{idProceso}/cancel")
    public ResponseEntity<CopyCancelResponseDto> cancel(@PathVariable String idProceso) {
        return ResponseEntity.ok(cancelPort.cancelar(idProceso));
    }

    /** DELETE /api/kardex/weighted-average/copy/{idProceso}/cleanup */
    @DeleteMapping("/{idProceso}/cleanup")
    public ResponseEntity<Void> cleanup(@PathVariable String idProceso) {
        cleanupPort.limpiar(idProceso);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DuplicateCopyJobException.class)
    public ResponseEntity<String> handleNotFound(DuplicateCopyJobException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    private HttpStatus resolverHttpStatus(String estado) {
        if (estado == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (estado) {
            case "COMPLETADO", "COMPLETADO_CON_ADVERTENCIAS" -> HttpStatus.OK;
            case "ERROR_NO_REINTENTABLE" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "ERROR_REINTENTABLE" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.OK;
        };
    }
}
