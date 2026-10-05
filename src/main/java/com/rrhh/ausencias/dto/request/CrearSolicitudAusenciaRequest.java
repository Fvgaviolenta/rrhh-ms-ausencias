package com.rrhh.ausencias.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CrearSolicitudAusenciaRequest(
        @NotNull LocalDate fecha,
        @NotBlank String tipo,
        @NotBlank String motivo
) {
}
