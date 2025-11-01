package com.kardex.infrastructure.adapters.output.remoteSync.config;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface IConfigClient {
    
    @GetExchange("/api/config/accounting-calendar/exists/{enterpriseId}")
    boolean existsDate(
        @PathVariable String enterpriseId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}