package com.rrhh.ausencias.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RechazarAusenciaFrontendRequest(
        @NotBlank String motivo
) {
}
