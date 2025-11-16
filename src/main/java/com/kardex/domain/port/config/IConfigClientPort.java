package com.kardex.domain.port.config;

import java.time.LocalDate;

public interface IConfigClientPort {
    boolean isValidAccountingDate(String enterpriseId, LocalDate date);
}