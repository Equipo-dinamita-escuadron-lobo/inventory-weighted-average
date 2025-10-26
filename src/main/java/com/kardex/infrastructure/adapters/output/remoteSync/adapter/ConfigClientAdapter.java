package com.kardex.infrastructure.adapters.output.remoteSync.adapter;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.kardex.domain.port.config.IConfigClientPort;
import com.kardex.infrastructure.adapters.output.remoteSync.config.IConfigClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConfigClientAdapter implements IConfigClientPort {

    private final IConfigClient configClient;

    @Override
    public boolean isValidAccountingDate(String enterpriseId, LocalDate date) {
        return configClient.existsDate(enterpriseId, date);
    }
}