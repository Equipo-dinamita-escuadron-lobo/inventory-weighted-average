package com.kardex.copy.application.services;

import com.kardex.copy.application.input.IExecuteKardexWaCopyPhasePort;
import com.kardex.copy.application.output.*;
import com.kardex.copy.domain.enums.CopyEstado;
import com.kardex.copy.domain.models.CopyJobLog;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyEquivalenciaDto;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyPhaseResponseDto;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio que orquesta la copia del módulo kardex weighted-average.
 *
 * Estrategia de copia (sin DetailOutput — WA es más simple que PEPS, ADR-39):
 * 1. Copiar ProductEntity del entOrigen → entDestino, remapeando idProduct (FK externa → PRODUCTS)
 *    usando equivalenciasPrev tabla "product".
 * 2. Para cada ProductEntity origen, copiar sus KardexEntity → nuevo ProductEntity destino.
 *
 * ProductEntity usa enterpriseId (no @TenantId) — se setea manualmente.
 * KardexEntity no tiene campo tenant propio — queda implícitamente bajo el ProductEntity destino.
 *
 * REQ-INVWA-01, REQ-INVWA-02, ADR-38, ADR-43 (KARDEX → {PRODUCTS}).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CopyKardexWaService implements IExecuteKardexWaCopyPhasePort {

    private static final String MODULO = "kardex-wa";

    private final ICopyJobLogRepositoryPort logRepo;
    private final IProductCopySourceRepositoryPort productSourceRepo;
    private final IProductCopyTargetRepositoryPort productTargetRepo;
    private final IKardexCopySourceRepositoryPort kardexSourceRepo;
    private final IKardexCopyTargetRepositoryPort kardexTargetRepo;

    @Override
    public CopyPhaseResponseDto ejecutar(CopyPhaseRequestDto request) {
        if (request.getEntOrigen().equals(request.getEntDestino())) {
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_NO_REINTENTABLE")
                    .mensaje("entOrigen y entDestino no pueden ser iguales")
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        }

        String idProceso = request.getIdProceso().toString();

        Optional<CopyJobLog> previo = logRepo.buscarPorIdProcesoYFase(idProceso, request.getFase());
        if (previo.isPresent()) {
            log.info("Fase {} del proceso {} ya fue ejecutada — retornando resultado previo (idempotencia)",
                    request.getFase(), idProceso);
            return construirResponseDesdeLog(previo.get());
        }

        CopyJobLog logInicio = CopyJobLog.builder()
                .idProceso(request.getIdProceso())
                .fase(request.getFase())
                .modulo(MODULO)
                .estado(CopyEstado.EN_PROCESO)
                .fechaInicio(Instant.now())
                .equivalenciasGeneradas(0)
                .build();
        logRepo.guardar(logInicio);

        // Índice idProduct externo (tabla "product") para remap
        Map<String, Long> productIndex = construirIndiceProducto(request.getEquivalenciasPrev());

        List<String> advertencias = new ArrayList<>();
        List<CopyEquivalenciaDto> equivalencias = new ArrayList<>();
        int totalRegistros = 0;

        try {
            // Paso 1: Copiar ProductEntity → construir mapa productEntityOldId → nueva ProductEntity
            List<ProductEntity> productsOrigen = productSourceRepo.findByEnterpriseId(request.getEntOrigen());
            Map<Long, ProductEntity> productEntityMap = new HashMap<>(); // oldProductEntityId → nueva ProductEntity

            for (ProductEntity original : productsOrigen) {
                ProductEntity nuevo = new ProductEntity();
                nuevo.setId(null);
                nuevo.setName(original.getName());
                nuevo.setReference(original.getReference());
                nuevo.setPresentation(original.getPresentation());
                nuevo.setManager(original.getManager());
                nuevo.setEnterpriseId(request.getEntDestino()); // manual — no @TenantId
                nuevo.setKardexList(Collections.emptyList()); // se setea después

                // Remapear idProduct externo (FK a PRODUCTS)
                nuevo.setIdProduct(
                    remapearIdProduct(original.getIdProduct(), original.getId(), advertencias, productIndex));

                ProductEntity guardado = productTargetRepo.guardar(nuevo);
                productEntityMap.put(original.getId(), guardado);

                equivalencias.add(CopyEquivalenciaDto.builder()
                        .modulo(MODULO)
                        .tabla("product_entity")
                        .idViejo(String.valueOf(original.getId()))
                        .idNuevo(String.valueOf(guardado.getId()))
                        .build());
                totalRegistros++;
            }

            // Paso 2: Copiar KardexEntity por cada ProductEntity
            for (ProductEntity original : productsOrigen) {
                ProductEntity nuevaProductEntity = productEntityMap.get(original.getId());
                if (nuevaProductEntity == null) continue;

                List<KardexEntity> kardexOrigen = kardexSourceRepo.findByProductId(original.getId());

                for (KardexEntity kardexOriginal : kardexOrigen) {
                    KardexEntity nuevoKardex = new KardexEntity();
                    nuevoKardex.setId(null);
                    nuevoKardex.setQuantity(kardexOriginal.getQuantity());
                    nuevoKardex.setUnitPrice(kardexOriginal.getUnitPrice());
                    nuevoKardex.setDetails(kardexOriginal.getDetails());
                    nuevoKardex.setType(kardexOriginal.getType());
                    nuevoKardex.setBalanceQuantity(kardexOriginal.getBalanceQuantity());
                    nuevoKardex.setBalanceUnitPrice(kardexOriginal.getBalanceUnitPrice());
                    nuevoKardex.setDate(kardexOriginal.getDate());
                    nuevoKardex.setProduct(nuevaProductEntity); // asociar al nuevo ProductEntity

                    kardexTargetRepo.guardar(nuevoKardex);
                    totalRegistros++;
                }
            }

        } catch (Exception e) {
            log.error("Error inesperado durante copia kardex-wa del proceso {}: {}", idProceso, e.getMessage(), e);
            registrarFallo(request, e.getMessage(), logInicio.getFechaInicio());
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_REINTENTABLE")
                    .mensaje("Error interno: " + e.getMessage())
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        }

        CopyEstado estadoFinal = advertencias.isEmpty()
                ? CopyEstado.COMPLETADO
                : CopyEstado.COMPLETADO_CON_ADVERTENCIAS;

        CopyJobLog logFin = CopyJobLog.builder()
                .idProceso(request.getIdProceso())
                .fase(request.getFase())
                .modulo(MODULO)
                .estado(estadoFinal)
                .fechaInicio(logInicio.getFechaInicio())
                .fechaFin(Instant.now())
                .equivalenciasGeneradas(equivalencias.size())
                .build();
        logRepo.guardar(logFin);

        return CopyPhaseResponseDto.builder()
                .estado(estadoFinal.name())
                .registrosProcesados(totalRegistros)
                .equivalenciasGeneradas(equivalencias)
                .mensaje("Copia kardex weighted-average completada exitosamente")
                .advertencias(advertencias)
                .build();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private Map<String, Long> construirIndiceProducto(List<CopyEquivalenciaDto> equivalenciasPrev) {
        if (equivalenciasPrev == null) return Collections.emptyMap();
        return equivalenciasPrev.stream()
                .filter(e -> "product".equals(e.getTabla())
                             && e.getIdViejo() != null && e.getIdNuevo() != null)
                .collect(Collectors.toMap(
                        CopyEquivalenciaDto::getIdViejo,
                        e -> Long.parseLong(e.getIdNuevo()),
                        (a, b) -> a));
    }

    private Long remapearIdProduct(Long idViejo, Long productEntityId,
                                    List<String> advertencias,
                                    Map<String, Long> productIndex) {
        if (idViejo == null) return null;

        Long idNuevo = productIndex.get(String.valueOf(idViejo));
        if (idNuevo == null) {
            String adv = String.format(
                "ProductEntity id=%s: idProduct=%s sin equivalencia en PRODUCTS; se inserta null.",
                productEntityId, idViejo);
            log.warn(adv);
            advertencias.add(adv);
        }
        return idNuevo;
    }

    private CopyPhaseResponseDto construirResponseDesdeLog(CopyJobLog log) {
        return CopyPhaseResponseDto.builder()
                .estado(log.getEstado().name())
                .registrosProcesados(log.getEquivalenciasGeneradas() != null ? log.getEquivalenciasGeneradas() : 0)
                .equivalenciasGeneradas(Collections.emptyList())
                .mensaje("Resultado de ejecución previa (idempotencia)")
                .advertencias(Collections.emptyList())
                .build();
    }

    private void registrarFallo(CopyPhaseRequestDto request, String mensaje, Instant fechaInicio) {
        try {
            logRepo.guardar(CopyJobLog.builder()
                    .idProceso(request.getIdProceso())
                    .fase(request.getFase())
                    .modulo(MODULO)
                    .estado(CopyEstado.FALLIDO)
                    .fechaInicio(fechaInicio != null ? fechaInicio : Instant.now())
                    .fechaFin(Instant.now())
                    .equivalenciasGeneradas(0)
                    .errorMessage(mensaje)
                    .build());
        } catch (Exception e) {
            log.error("Error al registrar fallo de copia kardex-wa: {}", e.getMessage());
        }
    }
}
