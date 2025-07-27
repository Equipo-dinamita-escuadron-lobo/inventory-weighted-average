package com.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kardex.application.ports.input.IProductCommandPort;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average/sync")
public class ProductSyncController {
    private final IProductCommandPort productCommandPort;

    @GetMapping("/test")
    public void test() {
        productCommandPort.test2("Hello from ProductSyncController");
    }
}
