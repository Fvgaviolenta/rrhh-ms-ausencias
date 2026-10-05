package com.rrhh.ausencias.dto.request;

import com.rrhh.ausencias.model.EstadoSolicitudAusencia;
import jakarta.validation.constraints.NotNull;

public record EvaluarSolicitudAusenciaRequest(
        @NotNull EstadoSolicitudAusencia estado,
        String motivoRechazo
) {
}
