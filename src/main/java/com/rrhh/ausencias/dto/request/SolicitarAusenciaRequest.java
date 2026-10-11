package com.rrhh.ausencias.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SolicitarAusenciaRequest(
        @NotBlank(message = "El trabajador_id es obligatorio")
        String trabajadorId,
        
        @NotBlank(message = "El tipo de ausencia es obligatorio")
        String tipo,
        
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,
        
        @NotNull(message = "La fecha de término es obligatoria")
        LocalDate fechaFin,
        
        String motivo
) {
}
