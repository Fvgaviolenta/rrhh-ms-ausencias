package com.rrhh.ausencias.controller;

import com.rrhh.ausencias.dto.ApiResponse;
import com.rrhh.ausencias.dto.request.RechazarAusenciaRequest;
import com.rrhh.ausencias.dto.request.SolicitarAusenciaRequest;
import com.rrhh.ausencias.dto.response.ServiceStatusResponse;
import com.rrhh.ausencias.dto.response.SolicitudAusenciaResponse;
import com.rrhh.ausencias.security.Roles;
import com.rrhh.ausencias.service.SolicitudAusenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/ausencias/trabajador/{trabajador_id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SolicitudAusenciaResponse>> listarPorTrabajador(
            @PathVariable("trabajador_id") String trabajadorId) {
        return ApiResponse.ok(solicitudAusenciaService.listarPorTrabajador(trabajadorId), "Solicitudes del trabajador");
    }

    @GetMapping("/ausencias/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SolicitudAusenciaResponse> obtener(@PathVariable("id") String id) {
        return ApiResponse.ok(solicitudAusenciaService.obtener(id), "Solicitud encontrada");
    }

    @GetMapping("/ausencias/mis-solicitudes")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SolicitudAusenciaResponse>> misSolicitudes() {
        return ApiResponse.ok(solicitudAusenciaService.misSolicitudes(), "Mis solicitudes de ausencia");
    }

    @PostMapping("/ausencias/solicitar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SolicitudAusenciaResponse>> solicitar(
            @Valid @RequestBody SolicitarAusenciaRequest request) {
        SolicitudAusenciaResponse creada = solicitudAusenciaService.solicitar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(creada, "Solicitud de ausencia creada"));
    }

    @PatchMapping("/ausencias/{id}/aprobar")
    @PreAuthorize("hasAnyRole('" + Roles.ADMIN_RRHH + "','" + Roles.JEFATURA + "')")
    public ApiResponse<SolicitudAusenciaResponse> aprobar(@PathVariable("id") String id) {
        return ApiResponse.ok(solicitudAusenciaService.aprobar(id), "Solicitud aprobada");
    }

    @PatchMapping("/ausencias/{id}/rechazar")
    @PreAuthorize("hasAnyRole('" + Roles.ADMIN_RRHH + "','" + Roles.JEFATURA + "')")
    public ApiResponse<SolicitudAusenciaResponse> rechazar(
            @PathVariable("id") String id,
            @Valid @RequestBody RechazarAusenciaRequest request) {
        return ApiResponse.ok(solicitudAusenciaService.rechazar(id, request), "Solicitud rechazada");
    }

    @GetMapping("/ausencias/status")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ServiceStatusResponse> status() {
        return ApiResponse.ok(solicitudAusenciaService.status(), "Estado del servicio de ausencias");
    }
}
