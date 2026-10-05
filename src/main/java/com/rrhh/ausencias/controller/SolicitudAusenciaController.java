package com.rrhh.ausencias.controller;

import com.rrhh.ausencias.dto.ApiResponse;
import com.rrhh.ausencias.dto.request.CrearSolicitudAusenciaRequest;
import com.rrhh.ausencias.dto.request.EvaluarSolicitudAusenciaRequest;
import com.rrhh.ausencias.dto.request.RechazarAusenciaFrontendRequest;
import com.rrhh.ausencias.dto.request.SolicitarAusenciaFrontendRequest;
import com.rrhh.ausencias.dto.response.ServiceStatusResponse;
import com.rrhh.ausencias.dto.response.SolicitudAusenciaResponse;
import com.rrhh.ausencias.service.SolicitudAusenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/solicitudes-ausencia/mias")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SolicitudAusenciaResponse>> listarMias() {
        return ApiResponse.ok(solicitudAusenciaService.listarMias(), "Solicitudes del trabajador");
    }

    @PostMapping("/solicitudes-ausencia")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ApiResponse<SolicitudAusenciaResponse>> crear(
            @Valid @RequestBody CrearSolicitudAusenciaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(solicitudAusenciaService.crear(request), "Solicitud enviada"));
    }

    @PatchMapping("/solicitudes-ausencia/{solicitud_id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN_RRHH','JEFATURA')")
    public ApiResponse<SolicitudAusenciaResponse> evaluar(
            @PathVariable("solicitud_id") String solicitudId,
            @Valid @RequestBody EvaluarSolicitudAusenciaRequest request
    ) {
        return ApiResponse.ok(solicitudAusenciaService.evaluar(solicitudId, request), "Solicitud evaluada");
    }

    @GetMapping("/ausencias/mis-solicitudes")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SolicitudAusenciaResponse>> misSolicitudes() {
        return listarMias();
    }

    @GetMapping("/ausencias/trabajador/{trabajador_id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN_RRHH','JEFATURA')")
    public ApiResponse<List<SolicitudAusenciaResponse>> porTrabajador(@PathVariable("trabajador_id") String trabajadorId) {
        return ApiResponse.ok(solicitudAusenciaService.listarPorTrabajador(trabajadorId), "Solicitudes del trabajador");
    }

    @GetMapping("/ausencias/{solicitud_id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SolicitudAusenciaResponse> obtener(@PathVariable("solicitud_id") String solicitudId) {
        return ApiResponse.ok(solicitudAusenciaService.obtener(solicitudId), "Solicitud de ausencia");
    }

    @PostMapping("/ausencias/solicitar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SolicitudAusenciaResponse>> solicitar(
            @Valid @RequestBody SolicitarAusenciaFrontendRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(solicitudAusenciaService.solicitar(request), "Solicitud enviada"));
    }

    @PatchMapping("/ausencias/{solicitud_id}/aprobar")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN_RRHH','JEFATURA')")
    public ApiResponse<SolicitudAusenciaResponse> aprobar(@PathVariable("solicitud_id") String solicitudId) {
        return ApiResponse.ok(solicitudAusenciaService.aprobar(solicitudId), "Solicitud aprobada");
    }

    @PatchMapping("/ausencias/{solicitud_id}/rechazar")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN_RRHH','JEFATURA')")
    public ApiResponse<SolicitudAusenciaResponse> rechazar(
            @PathVariable("solicitud_id") String solicitudId,
            @Valid @RequestBody RechazarAusenciaFrontendRequest request
    ) {
        return ApiResponse.ok(solicitudAusenciaService.rechazar(solicitudId, request.motivo()), "Solicitud rechazada");
    }

    @GetMapping("/ausencias/status")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ServiceStatusResponse> status() {
        return ApiResponse.ok(solicitudAusenciaService.status(), "Estado del servicio de ausencias");
    }
}
