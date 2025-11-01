package com.kardex.infrastructure.adapters.output.remoteSync.adapter;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.kardex.domain.port.common.IFormatterResultOutputPort;
import com.kardex.domain.port.config.IConfigClientPort;
import com.kardex.infrastructure.adapters.output.remoteSync.config.IConfigClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigClientAdapter implements IConfigClientPort {

    private final IConfigClient configClient;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public boolean isValidAccountingDate(String enterpriseId, LocalDate date) {
        try {
            return configClient.existsDate(enterpriseId, date);
        } catch (WebClientResponseException.ServiceUnavailable e) {
            log.warn("Configuration service is unavailable (503)");
            formatterResultOutputPort.returnErrorGenericResponse(500,"Configuration service is currently unavailable");
        } catch (WebClientResponseException e) {
            log.warn("Error calling configuration service: {} - {}", e.getStatusCode(), e.getStatusText());
            formatterResultOutputPort.returnErrorGenericResponse(500, "Error communicating with configuration service");
        } catch (Exception e) {
            log.warn("Unexpected error calling configuration service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500, "Unexpected error communicating with configuration service");
        }
        return false;
    }
}