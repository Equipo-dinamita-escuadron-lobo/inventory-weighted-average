package com.kardex.copy.application.services;

import com.kardex.copy.application.input.IExecuteKardexWaCopyPhasePort;
import com.kardex.copy.application.output.*;
import com.kardex.copy.domain.enums.CopyEstado;
import com.kardex.copy.domain.models.CopyJobLog;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyEquivalenciaDto;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.CopyPhaseResponseDto;
import com.kardex.domain.model.MovementType;
import com.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import com.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
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
        if (request.getDatosImportados() != null) {
            return ejecutarImportacion(request);
        }
        if (request.getEntDestino() == null || request.getEntDestino().isBlank()) {
            return ejecutarExportacion(request);
        }
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
                nuevo.setEnterpriseId(request.getEntDestino()); // manual — no @TenantId

                // Remapear productId externo (FK a PRODUCTS)
                nuevo.setProductId(
                    remapearIdProduct(original.getProductId(), original.getId(), advertencias, productIndex));

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
                    nuevoKardex.setProductId(nuevaProductEntity.getId()); // asociar al nuevo ProductEntity

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

    // ----------------------------------------------------------------
    // Modo BACKUP: exportar datos del entOrigen como payload serializable
    // ----------------------------------------------------------------

    private CopyPhaseResponseDto ejecutarExportacion(CopyPhaseRequestDto request) {
        log.info("Modo BACKUP — exportando kardex-wa para entOrigen={}", request.getEntOrigen());
        try {
            List<ProductEntity> products = productSourceRepo.findByEnterpriseId(request.getEntOrigen());

            List<Map<String, Object>> productsMaps = new ArrayList<>();
            List<Map<String, Object>> kardexMaps = new ArrayList<>();

            for (ProductEntity p : products) {
                Map<String, Object> pm = new LinkedHashMap<>();
                pm.put("id", p.getId());
                pm.put("productId", p.getProductId());
                pm.put("reference", p.getReference());
                pm.put("name", p.getName());
                pm.put("presentation", p.getPresentation());
                pm.put("enterpriseId", p.getEnterpriseId());
                pm.put("state", p.isState());
                productsMaps.add(pm);

                List<KardexEntity> kardexList = kardexSourceRepo.findByProductId(p.getId());
                for (KardexEntity k : kardexList) {
                    Map<String, Object> km = new LinkedHashMap<>();
                    km.put("id", k.getId());
                    km.put("productEntityId", p.getId()); // FK local → ProductEntity
                    km.put("factCode", k.getFactCode());
                    km.put("quantity", k.getQuantity());
                    km.put("unitPrice", k.getUnitPrice() != null ? k.getUnitPrice().toPlainString() : null);
                    km.put("details", k.getDetails());
                    km.put("type", k.getType() != null ? k.getType().name() : null);
                    km.put("balanceQuantity", k.getBalanceQuantity());
                    km.put("balanceUnitPrice", k.getBalanceUnitPrice() != null ? k.getBalanceUnitPrice().toPlainString() : null);
                    km.put("totalBalance", k.getTotalBalance() != null ? k.getTotalBalance().toPlainString() : null);
                    km.put("date", k.getDate() != null ? k.getDate().toString() : null);
                    kardexMaps.add(km);
                }
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("products", productsMaps);
            payload.put("kardex", kardexMaps);

            int total = productsMaps.size() + kardexMaps.size();
            log.info("BACKUP kardex-wa: {} productos, {} movimientos exportados", productsMaps.size(), kardexMaps.size());

            return CopyPhaseResponseDto.builder()
                    .estado("COMPLETADO")
                    .registrosProcesados(total)
                    .equivalenciasGeneradas(Collections.emptyList())
                    .mensaje("Modo BACKUP — datos exportados correctamente")
                    .advertencias(Collections.emptyList())
                    .datosExportados(payload)
                    .build();
        } catch (Exception e) {
            log.error("Error durante exportación kardex-wa: {}", e.getMessage(), e);
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_REINTENTABLE")
                    .mensaje("Error en exportación: " + e.getMessage())
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        }
    }

    // ----------------------------------------------------------------
    // Modo RESTORE: importar datos desde payload serializado
    // ----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private CopyPhaseResponseDto ejecutarImportacion(CopyPhaseRequestDto request) {
        log.info("Modo RESTORE — importando kardex-wa para entDestino={}", request.getEntDestino());

        String idProceso = request.getIdProceso().toString();

        // Idempotencia
        Optional<CopyJobLog> previo = logRepo.buscarPorIdProcesoYFase(idProceso, request.getFase());
        if (previo.isPresent()) {
            log.info("RESTORE fase {} proceso {} ya ejecutada — retornando resultado previo", request.getFase(), idProceso);
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

        List<String> advertencias = new ArrayList<>();
        List<CopyEquivalenciaDto> equivalencias = new ArrayList<>();
        int totalRegistros = 0;

        try {
            Map<String, Object> datos = (Map<String, Object>) request.getDatosImportados();
            List<Map<String, Object>> productsMaps = (List<Map<String, Object>>) datos.get("products");
            List<Map<String, Object>> kardexMaps = (List<Map<String, Object>>) datos.get("kardex");

            Map<String, Long> productIndex = construirIndiceProducto(request.getEquivalenciasPrev());

            // Paso 1: importar ProductEntity
            Map<Long, Long> productEntityOldToNew = new HashMap<>(); // oldId → newId
            if (productsMaps != null) {
                for (Map<String, Object> pm : productsMaps) {
                    Long oldId = toLong(pm.get("id"));
                    Long oldProductId = toLong(pm.get("productId"));

                    ProductEntity nuevo = new ProductEntity();
                    nuevo.setId(null);
                    nuevo.setName(toStr(pm.get("name")));
                    nuevo.setReference(toStr(pm.get("reference")));
                    nuevo.setPresentation(toStr(pm.get("presentation")));
                    nuevo.setEnterpriseId(request.getEntDestino() != null
                            ? request.getEntDestino()
                            : toStr(pm.get("enterpriseId")));
                    nuevo.setState(toBool(pm.get("state")));
                    nuevo.setProductId(remapearProductId(oldProductId, productIndex, advertencias, oldId));

                    ProductEntity guardado = productTargetRepo.guardar(nuevo);
                    if (oldId != null) {
                        productEntityOldToNew.put(oldId, guardado.getId());
                    }

                    equivalencias.add(CopyEquivalenciaDto.builder()
                            .modulo(MODULO)
                            .tabla("product_entity")
                            .idViejo(String.valueOf(oldId))
                            .idNuevo(String.valueOf(guardado.getId()))
                            .build());
                    totalRegistros++;
                }
            }

            // Paso 2: importar KardexEntity
            if (kardexMaps != null) {
                for (Map<String, Object> km : kardexMaps) {
                    Long oldProductEntityId = toLong(km.get("productEntityId"));
                    Long newProductEntityId = oldProductEntityId != null
                            ? productEntityOldToNew.get(oldProductEntityId)
                            : null;

                    if (newProductEntityId == null) {
                        String adv = "KardexEntity: productEntityId=" + oldProductEntityId + " sin equivalencia; omitido.";
                        log.warn(adv);
                        advertencias.add(adv);
                        continue;
                    }

                    KardexEntity nuevo = new KardexEntity();
                    nuevo.setId(null);
                    nuevo.setProductId(newProductEntityId);
                    nuevo.setFactCode(toStr(km.get("factCode")));
                    nuevo.setQuantity(toInt(km.get("quantity")));
                    nuevo.setUnitPrice(toBigDecimal(km.get("unitPrice")));
                    nuevo.setDetails(toStr(km.get("details")));
                    nuevo.setType(toMovementType(km.get("type")));
                    nuevo.setBalanceQuantity(toInt(km.get("balanceQuantity")));
                    nuevo.setBalanceUnitPrice(toBigDecimal(km.get("balanceUnitPrice")));
                    nuevo.setTotalBalance(toBigDecimal(km.get("totalBalance")));
                    nuevo.setDate(toZonedDateTime(km.get("date")));

                    kardexTargetRepo.guardar(nuevo);
                    totalRegistros++;
                }
            }

        } catch (Exception e) {
            log.error("Error durante importación kardex-wa proceso {}: {}", idProceso, e.getMessage(), e);
            registrarFallo(request, e.getMessage(), logInicio.getFechaInicio());
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_REINTENTABLE")
                    .mensaje("Error en importación: " + e.getMessage())
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
                .mensaje("RESTORE kardex weighted-average completado")
                .advertencias(advertencias)
                .build();
    }

    // ----------------------------------------------------------------
    // Helpers de conversión de tipos (JSON → Java)
    // ----------------------------------------------------------------

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException ignored) { return null; }
    }

    private int toInt(Object v) {
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException ignored) { return 0; }
    }

    private String toStr(Object v) {
        return v != null ? v.toString() : null;
    }

    private boolean toBool(Object v) {
        return v instanceof Boolean b && b;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException ignored) { return null; }
    }

    private ZonedDateTime toZonedDateTime(Object v) {
        if (v == null) return null;
        try { return ZonedDateTime.parse(v.toString()); } catch (Exception ignored) { return null; }
    }

    private MovementType toMovementType(Object v) {
        if (v == null) return null;
        try { return MovementType.valueOf(v.toString()); } catch (IllegalArgumentException ignored) { return null; }
    }

    private Long remapearProductId(Long idViejo, Map<String, Long> productIndex,
                                    List<String> advertencias, Long entidadId) {
        if (idViejo == null) return null;
        Long idNuevo = productIndex.get(String.valueOf(idViejo));
        if (idNuevo == null) {
            String adv = "productId=" + idViejo + " sin equivalencia para ProductEntity id=" + entidadId;
            log.warn(adv);
            advertencias.add(adv);
        }
        return idNuevo;
    }
}
