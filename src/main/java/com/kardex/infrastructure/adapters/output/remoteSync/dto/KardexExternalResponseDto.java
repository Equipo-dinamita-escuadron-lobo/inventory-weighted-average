package com.kardex.infrastructure.adapters.output.remoteSync.dto;

import java.util.List;

import com.kardex.infrastructure.adapters.input.rest.dto.request.KardexBatchDtoRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for external kardex service response
 * 
 * Represents the response structure from the external microservice
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexExternalResponseDto {
    
    private List<KardexBatchDtoRequest> data;
    
    private int status;
    
    private String message;
}
