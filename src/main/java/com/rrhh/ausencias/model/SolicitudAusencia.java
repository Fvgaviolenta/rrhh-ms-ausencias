package com.rrhh.ausencias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "ausencias_solicitud_ausencia")
public class SolicitudAusencia {
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String tenantId;

    @Column(name = "trabajador_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String trabajadorId;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSolicitudAusencia estado = EstadoSolicitudAusencia.PENDIENTE;

    @Column(length = 500)
    private String motivo;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
}
