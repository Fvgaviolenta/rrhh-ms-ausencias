package com.rrhh.ausencias.dto.response;

import com.rrhh.ausencias.model.EstadoSolicitudAusencia;

import java.time.Instant;
import java.time.LocalDate;

public record SolicitudAusenciaResponse(
        String id,
        String tenantId,
        String trabajadorId,
        String tipo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoSolicitudAusencia estado,
        String motivo,
        Instant creadoEn
) {}
