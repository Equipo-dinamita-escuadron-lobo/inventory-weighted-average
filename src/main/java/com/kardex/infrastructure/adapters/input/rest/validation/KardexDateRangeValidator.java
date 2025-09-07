package com.kardex.infrastructure.adapters.input.rest.validation;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kardex.domain.port.IMessageServicePort;
import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexFilterDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class KardexDateRangeValidator implements ConstraintValidator<ValidDateRange, KardexFilterDto> {

    @Autowired
    private IMessageServicePort messageService;

    @Override
    public boolean isValid(KardexFilterDto value, ConstraintValidatorContext context) {
        if (value == null) return true;

        LocalDate start = value.getStartDate();
        LocalDate end = value.getEndDate();

        // Ambos nulos 
        if (start == null && end == null) return true;

        // Uno nulo → error
        if (start == null || end == null) {
            context.disableDefaultConstraintViolation();
            String message = messageService.getMessage(
                "kardex.validation.date.range.incomplete", 
                "Ambas fechas deben estar presentes o ambas deben ser nulas"
            );
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        // end >= start
        if (end.isBefore(start)) {
            context.disableDefaultConstraintViolation();
            String message = messageService.getMessage(
                "kardex.validation.date.range.invalid", 
                "La fecha final no puede ser anterior a la fecha inicial"
            );
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        return true;
    }
}
