package com.kardex.copy.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kardex.copy.application.input.*;
import com.kardex.copy.infrastructure.adapters.input.rest.controller.CopyKardexWaController;
import com.kardex.copy.infrastructure.adapters.input.rest.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TDD RED → GREEN: tests del controlador REST CopyKardexWaController.
 * REQ-INVWA-03.
 */
@ExtendWith(MockitoExtension.class)
class CopyKardexWaControllerTest {

    @Mock
    private IExecuteKardexWaCopyPhasePort executePort;
    @Mock
    private IGetKardexWaCopyStatusPort statusPort;
    @Mock
    private ICancelKardexWaCopyPort cancelPort;
    @Mock
    private ICleanupKardexWaCopyPort cleanupPort;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        CopyKardexWaController controller = new CopyKardexWaController(
                executePort, statusPort, cancelPort, cleanupPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/kardex/weighted-average/copy/phase retorna 200 COMPLETADO")
    void executePhase_happy_200() throws Exception {
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(UUID.randomUUID())
                .fase(3)
                .entOrigen("A")
                .entDestino("B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(Collections.emptyList())
                .build();

        when(executePort.ejecutar(any())).thenReturn(CopyPhaseResponseDto.builder()
                .estado("COMPLETADO")
                .registrosProcesados(10)
                .equivalenciasGeneradas(Collections.emptyList())
                .mensaje("Copia kardex weighted-average completada exitosamente")
                .advertencias(Collections.emptyList())
                .build());

        mockMvc.perform(post("/api/kardex/weighted-average/copy/phase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.registrosProcesados").value(10));
    }

    @Test
    @DisplayName("POST /api/kardex/weighted-average/copy/phase con advertencias retorna 200")
    void executePhase_conAdvertencias_200() throws Exception {
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(UUID.randomUUID())
                .fase(3)
                .entOrigen("A")
                .entDestino("B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(Collections.emptyList())
                .build();

        when(executePort.ejecutar(any())).thenReturn(CopyPhaseResponseDto.builder()
                .estado("COMPLETADO_CON_ADVERTENCIAS")
                .registrosProcesados(2)
                .equivalenciasGeneradas(Collections.emptyList())
                .advertencias(Collections.singletonList("ProductEntity id=1: idProduct=100 sin equivalencia; se inserta null."))
                .build());

        mockMvc.perform(post("/api/kardex/weighted-average/copy/phase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO_CON_ADVERTENCIAS"));
    }

    @Test
    @DisplayName("GET /api/kardex/weighted-average/copy/{idProceso}/status retorna 200")
    void getStatus_happy_200() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(statusPort.obtenerEstado(idProceso)).thenReturn(CopyStatusResponseDto.builder()
                .fase(3).estado("COMPLETADO").registrosProcesados(10).intentos(1).build());

        mockMvc.perform(get("/api/kardex/weighted-average/copy/{idProceso}/status", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    @DisplayName("POST /api/kardex/weighted-average/copy/{idProceso}/cancel retorna 200 CANCELADO")
    void cancel_happy_200() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(cancelPort.cancelar(idProceso)).thenReturn(CopyCancelResponseDto.builder()
                .estado("CANCELADO")
                .mensaje("Proceso de copia kardex weighted-average cancelado exitosamente")
                .build());

        mockMvc.perform(post("/api/kardex/weighted-average/copy/{idProceso}/cancel", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }
}
