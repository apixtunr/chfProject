package com.lacasadelchef.erp.inicio;

import com.lacasadelchef.erp.inicio.dto.PanelInicioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inicio")
@RequiredArgsConstructor
public class InicioController {

    private final InicioService inicioService;

    @GetMapping
    public PanelInicioResponse panel() {
        return inicioService.panel();
    }
}
