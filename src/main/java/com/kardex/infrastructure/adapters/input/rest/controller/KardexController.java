package com.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kardex.application.ports.input.IKardexCommandPort;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex")
public class KardexController {

    private final IKardexCommandPort kardexCommandPort;

    @GetMapping
    public ResponseEntity<?> test(){
        kardexCommandPort.test("Hola");
        return ResponseEntity.ok("Hola");
    }
    
}
