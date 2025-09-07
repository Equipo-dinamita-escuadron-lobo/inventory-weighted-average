package com.kardex.application.service.kardex.query;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kardex.application.ports.input.IKardexQueryPort;
import com.kardex.domain.model.Kardex;
import com.kardex.domain.port.IKardexQueryRepositoryPort;
import com.kardex.domain.port.IMessageServicePort;
import com.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KardexQueryService implements IKardexQueryPort {

    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IMessageServicePort messageService;

    @Override
    public Page<Kardex> findProductId(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            log.info(messageService.getMessage(MessageKeys.LOG_KARDEX_QUERY_WITH_DATES, 
                productId, startDate, endDate));
            return kardexQueryRepositoryPort.findProductIdAndDate(productId, pageable, startDate, endDate);
        }
        
        log.info(messageService.getMessage(MessageKeys.LOG_KARDEX_QUERY_STARTED, productId));
        return kardexQueryRepositoryPort.findProductId(productId, pageable);
    }
    
}
