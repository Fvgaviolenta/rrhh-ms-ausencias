package com.rrhh.ausencias.controller;

import com.rrhh.ausencias.dto.ApiResponse;
import com.rrhh.ausencias.dto.response.ServiceStatusResponse;
import com.rrhh.ausencias.dto.response.SolicitudAusenciaResponse;
import com.rrhh.ausencias.service.SolicitudAusenciaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SolicitudAusenciaController {
    private final SolicitudAusenciaService solicitudAusenciaService;

    public SolicitudAusenciaController(SolicitudAusenciaService solicitudAusenciaService) {
        this.solicitudAusenciaService = solicitudAusenciaService;
    }

    @GetMapping("/solicitudes-ausencia")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN_RRHH','JEFATURA')")
    public ApiResponse<List<SolicitudAusenciaResponse>> listar() {
        return ApiResponse.ok(solicitudAusenciaService.listarPorTenant(), "Solicitudes de ausencia del tenant");
    }

    @GetMapping("/ausencias/status")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ServiceStatusResponse> status() {
        return ApiResponse.ok(solicitudAusenciaService.status(), "Estado del servicio de ausencias");
    }
}
