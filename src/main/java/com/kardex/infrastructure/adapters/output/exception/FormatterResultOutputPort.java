package com.kardex.infrastructure.adapters.output.exception;

import org.springframework.stereotype.Service;

import com.kardex.domain.port.IFormatterResultOutputPort;
import com.kardex.infrastructure.adapters.output.exception.handler.BusinessRuleException;


@Service
public class FormatterResultOutputPort implements IFormatterResultOutputPort {

    @Override
    public void returnResponseError(int status, String message) {
        throw new  BusinessRuleException(status, message);
    }
    
}
