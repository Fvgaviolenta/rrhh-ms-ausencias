package com.rrhh.ausencias.service;

import com.rrhh.ausencias.dto.request.CrearSolicitudAusenciaRequest;
import com.rrhh.ausencias.dto.request.EvaluarSolicitudAusenciaRequest;
import com.rrhh.ausencias.dto.request.SolicitarAusenciaFrontendRequest;
import com.rrhh.ausencias.dto.response.ServiceStatusResponse;
import com.rrhh.ausencias.dto.response.SolicitudAusenciaResponse;
import com.rrhh.ausencias.exception.DomainException;
import com.rrhh.ausencias.model.EstadoSolicitudAusencia;
import com.rrhh.ausencias.model.SolicitudAusencia;
import com.rrhh.ausencias.repository.SolicitudAusenciaRepository;
import com.rrhh.ausencias.security.Roles;
import com.rrhh.ausencias.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SolicitudAusenciaService {
    private static final String SERVICE_VERSION = "0.1.0";

    private final SolicitudAusenciaRepository solicitudAusenciaRepository;
    private final TenantContext tenantContext;

    public SolicitudAusenciaService(
            SolicitudAusenciaRepository solicitudAusenciaRepository,
            TenantContext tenantContext
    ) {
        this.solicitudAusenciaRepository = solicitudAusenciaRepository;
        this.tenantContext = tenantContext;
    }

    public List<SolicitudAusenciaResponse> listarPorTenant() {
        TenantContext.AuthenticatedUser actor = tenantContext.require();
        String authority = Roles.authorityFromClaim(actor.role());
        if (Roles.TRABAJADOR.equals(authority)) {
            throw new DomainException(403, "Un trabajador no puede listar solicitudes de ausencia del tenant");
        }
        return solicitudAusenciaRepository.findByTenantIdOrderByCreadoEnDesc(actor.tenantId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SolicitudAusenciaResponse> listarMias() {
        TenantContext.AuthenticatedUser actor = tenantContext.require();
        if (actor.trabajadorId() == null || actor.trabajadorId().isBlank()) {
            throw new DomainException(403, "El token no incluye trabajador_id");
        }
        return solicitudAusenciaRepository
                .findByTenantIdAndTrabajadorIdOrderByCreadoEnDesc(actor.tenantId(), actor.trabajadorId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SolicitudAusenciaResponse obtener(String solicitudId) {
        TenantContext.AuthenticatedUser actor = tenantContext.require();
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findByIdAndTenantId(solicitudId, actor.tenantId())
                .orElseThrow(() -> new DomainException(404, "Solicitud no encontrada"));
        String authority = Roles.authorityFromClaim(actor.role());
        if (Roles.TRABAJADOR.equals(authority) && !solicitud.getTrabajadorId().equals(actor.trabajadorId())) {
            throw new DomainException(403, "Un trabajador no ve solicitudes de otros");
        }
        return toResponse(solicitud);
    }

    public List<SolicitudAusenciaResponse> listarPorTrabajador(String trabajadorId) {
        TenantContext.AuthenticatedUser actor = tenantContext.require();
        String authority = Roles.authorityFromClaim(actor.role());
        if (Roles.TRABAJADOR.equals(authority)) {
            throw new DomainException(403, "Un trabajador no puede listar solicitudes de otro");
        }
        return solicitudAusenciaRepository
                .findByTenantIdAndTrabajadorIdOrderByCreadoEnDesc(actor.tenantId(), trabajadorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SolicitudAusenciaResponse solicitar(SolicitarAusenciaFrontendRequest request) {
        TenantContext.AuthenticatedUser actor = tenantContext.require();
        String authority = Roles.authorityFromClaim(actor.role());
        String trabajadorId;
        if (Roles.TRABAJADOR.equals(authority)) {
            if (actor.trabajadorId() == null || actor.trabajadorId().isBlank()) {
                throw new DomainException(403, "El token no incluye trabajador_id");
            }
            trabajadorId = actor.trabajadorId();
        } else {
            if (request.trabajadorId() == null || request.trabajadorId().isBlank()) {
                throw new DomainException(400, "El trabajador es obligatorio", "trabajadorId");
            }
            trabajadorId = request.trabajadorId();
        }
        LocalDate fin = request.fechaFin() != null ? request.fechaFin() : request.fechaInicio();
        if (fin.isBefore(request.fechaInicio())) {
            throw new DomainException(400, "La fecha fin no puede ser anterior al inicio", "fechaFin");
        }
        SolicitudAusencia solicitud = new SolicitudAusencia();
        solicitud.setId(UUID.randomUUID().toString());
        solicitud.setTenantId(actor.tenantId());
        solicitud.setTrabajadorId(trabajadorId);
        solicitud.setTipo(request.tipo().trim());
        solicitud.setFechaInicio(request.fechaInicio());
        solicitud.setFechaFin(fin);
        solicitud.setMotivo(request.motivo() == null ? null : request.motivo().trim());
        solicitud.setEstado(EstadoSolicitudAusencia.PENDIENTE);
        solicitud.setCreadoEn(Instant.now());
        return toResponse(solicitudAusenciaRepository.save(solicitud));
    }

    @Transactional
    public SolicitudAusenciaResponse aprobar(String solicitudId) {
        return evaluar(solicitudId, new EvaluarSolicitudAusenciaRequest(EstadoSolicitudAusencia.APROBADO, null));
    }

    @Transactional
    public SolicitudAusenciaResponse rechazar(String solicitudId, String motivo) {
        return evaluar(solicitudId, new EvaluarSolicitudAusenciaRequest(EstadoSolicitudAusencia.RECHAZADO, motivo));
    }

    @Transactional
    public SolicitudAusenciaResponse crear(CrearSolicitudAusenciaRequest request) {
        TenantContext.AuthenticatedUser actor = tenantContext.require();
        if (actor.trabajadorId() == null || actor.trabajadorId().isBlank()) {
            throw new DomainException(403, "El token no incluye trabajador_id");
        }
        SolicitudAusencia solicitud = new SolicitudAusencia();
        solicitud.setId(UUID.randomUUID().toString());
        solicitud.setTenantId(actor.tenantId());
        solicitud.setTrabajadorId(actor.trabajadorId());
        solicitud.setTipo(request.tipo().trim());
        solicitud.setFechaInicio(request.fecha());
        solicitud.setFechaFin(request.fecha());
        solicitud.setMotivo(request.motivo().trim());
        solicitud.setEstado(EstadoSolicitudAusencia.PENDIENTE);
        solicitud.setCreadoEn(Instant.now());
        return toResponse(solicitudAusenciaRepository.save(solicitud));
    }

    @Transactional
    public SolicitudAusenciaResponse evaluar(String solicitudId, EvaluarSolicitudAusenciaRequest request) {
        TenantContext.AuthenticatedUser actor = tenantContext.require();
        if (request.estado() != EstadoSolicitudAusencia.APROBADO && request.estado() != EstadoSolicitudAusencia.RECHAZADO) {
            throw new DomainException(400, "Solo se puede aprobar o rechazar", "estado");
        }
        if (request.estado() == EstadoSolicitudAusencia.RECHAZADO
                && (request.motivoRechazo() == null || request.motivoRechazo().isBlank())) {
            throw new DomainException(400, "El rechazo requiere un motivo", "motivo_rechazo");
        }
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findByIdAndTenantId(solicitudId, actor.tenantId())
                .orElseThrow(() -> new DomainException(404, "Solicitud no encontrada"));
        if (solicitud.getEstado() != EstadoSolicitudAusencia.PENDIENTE) {
            throw new DomainException(400, "La solicitud ya fue evaluada", "estado");
        }
        if (actor.trabajadorId() != null && actor.trabajadorId().equals(solicitud.getTrabajadorId())) {
            throw new DomainException(403, "No puedes evaluar tu propia ausencia");
        }
        solicitud.setEstado(request.estado());
        solicitud.setFechaEvaluacion(Instant.now());
        if (request.estado() == EstadoSolicitudAusencia.RECHAZADO) {
            solicitud.setMotivoRechazo(request.motivoRechazo().trim());
        }
        return toResponse(solicitudAusenciaRepository.save(solicitud));
    }

    public ServiceStatusResponse status() {
        return new ServiceStatusResponse("rrhh-ausencias", SERVICE_VERSION, "UP");
    }

    private SolicitudAusenciaResponse toResponse(SolicitudAusencia solicitud) {
        return new SolicitudAusenciaResponse(
                solicitud.getId(),
                solicitud.getTenantId(),
                solicitud.getTrabajadorId(),
                solicitud.getTipo(),
                solicitud.getFechaInicio(),
                solicitud.getFechaFin(),
                solicitud.getEstado(),
                solicitud.getMotivo(),
                solicitud.getMotivoRechazo(),
                solicitud.getFechaEvaluacion(),
                solicitud.getCreadoEn()
        );
    }
}
