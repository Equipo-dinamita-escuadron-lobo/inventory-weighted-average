package com.kardex.application.service.kardex.command;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.common.IMessageServicePort;
import com.kardex.domain.port.config.IConfigClientPort;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;
import com.kardex.infrastructure.adapters.output.exception.customized.BusinessRuleException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexDateValidationService {
    
    private final IConfigClientPort configClientPort;
    private final IMessageServicePort messageService;

    public void validateAndSetDate(Kardex kardex, String enterpriseId) {
        LocalDate today = LocalDate.now(ZoneId.of("America/Bogota"));
        
        if (!configClientPort.isValidAccountingDate(enterpriseId, today)) {
            String errorMessage = messageService.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
            throw new BusinessRuleException(400, errorMessage);
        }
        
        kardex.addDate();
    }
}