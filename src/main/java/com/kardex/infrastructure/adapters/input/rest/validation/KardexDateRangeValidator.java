package com.kardex.infrastructure.adapters.input.rest.validation;

import java.time.LocalDate;

import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexFilterDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class KardexDateRangeValidator implements ConstraintValidator<ValidDateRange, KardexFilterDto> {

    @Override
    public boolean isValid(KardexFilterDto value, ConstraintValidatorContext context) {
        if (value == null) return true;

        LocalDate start = value.getStartDate();
        LocalDate end = value.getEndDate();

        // Ambos nulos 
        if (start == null && end == null) return true;

        // Uno nulo → error
        if (start == null || end == null) return false;

        // end >= start
        return !end.isBefore(start);
    }
}
