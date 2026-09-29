package com.rrhh.ausencias.service;

import com.rrhh.ausencias.dto.request.RechazarAusenciaRequest;
import com.rrhh.ausencias.dto.request.SolicitarAusenciaRequest;
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

    public List<SolicitudAusenciaResponse> listarPorTrabajador(String trabajadorId) {
        String tenantId = tenantContext.require().tenantId();
        return solicitudAusenciaRepository.findByTrabajadorIdOrderByCreadoEnDesc(trabajadorId)
                .stream()
                .filter(s -> s.getTenantId().equals(tenantId))
                .map(this::toResponse)
                .toList();
    }

    public SolicitudAusenciaResponse obtener(String id) {
        String tenantId = tenantContext.require().tenantId();
        return solicitudAusenciaRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toResponse)
                .orElseThrow(() -> new DomainException(404, "Solicitud no encontrada"));
    }

    public List<SolicitudAusenciaResponse> misSolicitudes() {
        TenantContext.AuthenticatedUser actor = tenantContext.require();
        String trabajadorId = actor.trabajadorId();
        
        if (trabajadorId == null) {
            throw new DomainException(400, "El usuario no tiene trabajador_id asociado");
        }
        
        return solicitudAusenciaRepository.findByTrabajadorIdOrderByCreadoEnDesc(trabajadorId)
                .stream()
                .filter(s -> s.getTenantId().equals(actor.tenantId()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SolicitudAusenciaResponse solicitar(SolicitarAusenciaRequest request) {
        String tenantId = tenantContext.require().tenantId();
        
        // Validar fechas
        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new DomainException(400, "La fecha de término no puede ser anterior a la fecha de inicio");
        }
        
        // Validar que no exista una solicitud solapada
        List<SolicitudAusencia> solicitudesSolapadas = solicitudAusenciaRepository
                .findByTrabajadorIdAndEstadoOrderByCreadoEnDesc(
                        request.trabajadorId(),
                        EstadoSolicitudAusencia.PENDIENTE
                )
                .stream()
                .filter(s -> s.getTenantId().equals(tenantId))
                .filter(s -> fechasSolapadas(
                        s.getFechaInicio(),
                        s.getFechaFin(),
                        request.fechaInicio(),
                        request.fechaFin()
                ))
                .toList();
        
        if (!solicitudesSolapadas.isEmpty()) {
            throw new DomainException(409, "Ya existe una solicitud de ausencia solapada pendiente");
        }
        
        SolicitudAusencia solicitud = new SolicitudAusencia();
        solicitud.setId(UUID.randomUUID().toString());
        solicitud.setTenantId(tenantId);
        solicitud.setTrabajadorId(request.trabajadorId());
        solicitud.setTipo(request.tipo());
        solicitud.setFechaInicio(request.fechaInicio());
        solicitud.setFechaFin(request.fechaFin());
        solicitud.setEstado(EstadoSolicitudAusencia.PENDIENTE);
        solicitud.setMotivo(request.motivo());
        solicitud.setCreadoEn(java.time.Instant.now());
        
        solicitudAusenciaRepository.save(solicitud);
        
        // TODO: Publicar evento de solicitud creada en RabbitMQ
        
        return toResponse(solicitud);
    }

    @Transactional
    public SolicitudAusenciaResponse aprobar(String id) {
        String tenantId = tenantContext.require().tenantId();
        
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new DomainException(404, "Solicitud no encontrada"));
        
        if (solicitud.getEstado() != EstadoSolicitudAusencia.PENDIENTE) {
            throw new DomainException(400, "Solo se pueden aprobar solicitudes en estado PENDIENTE");
        }
        
        // Validar que no sea auto-aprobación
        String trabajadorId = tenantContext.require().trabajadorId();
        if (trabajadorId != null && trabajadorId.equals(solicitud.getTrabajadorId())) {
            throw new DomainException(403, "No puedes aprobar tu propia solicitud");
        }
        
        solicitud.setEstado(EstadoSolicitudAusencia.APROBADO);
        solicitudAusenciaRepository.save(solicitud);
        
        // TODO: Publicar evento de solicitud aprobada en RabbitMQ
        
        return toResponse(solicitud);
    }

    @Transactional
    public SolicitudAusenciaResponse rechazar(String id, RechazarAusenciaRequest request) {
        String tenantId = tenantContext.require().tenantId();
        
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new DomainException(404, "Solicitud no encontrada"));
        
        if (solicitud.getEstado() != EstadoSolicitudAusencia.PENDIENTE) {
            throw new DomainException(400, "Solo se pueden rechazar solicitudes en estado PENDIENTE");
        }
        
        solicitud.setEstado(EstadoSolicitudAusencia.RECHAZADO);
        solicitud.setMotivo(request.motivo());
        solicitudAusenciaRepository.save(solicitud);
        
        // TODO: Publicar evento de solicitud rechazada en RabbitMQ
        
        return toResponse(solicitud);
    }

    public ServiceStatusResponse status() {
        return new ServiceStatusResponse("rrhh-ausencias", SERVICE_VERSION, "OPERATIVO");
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
                solicitud.getCreadoEn()
        );
    }

    private boolean fechasSolapadas(LocalDate inicio1, LocalDate fin1, LocalDate inicio2, LocalDate fin2) {
        return !fin1.isBefore(inicio2) && !fin2.isBefore(inicio1);
    }
}
