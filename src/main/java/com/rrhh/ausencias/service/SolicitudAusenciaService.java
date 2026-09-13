package com.rrhh.ausencias.service;

import com.rrhh.ausencias.dto.response.ServiceStatusResponse;
import com.rrhh.ausencias.dto.response.SolicitudAusenciaResponse;
import com.rrhh.ausencias.exception.DomainException;
import com.rrhh.ausencias.model.SolicitudAusencia;
import com.rrhh.ausencias.repository.SolicitudAusenciaRepository;
import com.rrhh.ausencias.security.Roles;
import com.rrhh.ausencias.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

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
                solicitud.getCreadoEn()
        );
    }
}
