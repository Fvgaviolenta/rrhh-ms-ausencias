package com.rrhh.ausencias.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitarAusenciaFrontendRequest(
        String trabajadorId,
        @NotBlank String tipo,
        @NotNull LocalDate fechaInicio,
        LocalDate fechaFin,
        String motivo
) {
}
