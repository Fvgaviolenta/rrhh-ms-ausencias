package com.rrhh.ausencias.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RechazarAusenciaRequest(
        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String motivo
) {
}
