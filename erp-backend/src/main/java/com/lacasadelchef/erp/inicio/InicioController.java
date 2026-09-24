package com.lacasadelchef.erp.inicio;

import com.lacasadelchef.erp.inicio.dto.PanelInicioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pantalla de inicio. Una sola llamada arma todo el panel del usuario conectado, ya
 * recortado a lo que su rol puede ver: el frontend no decide que secciones pedir ni
 * hace una consulta por evento para saber que le falta.
 */
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
