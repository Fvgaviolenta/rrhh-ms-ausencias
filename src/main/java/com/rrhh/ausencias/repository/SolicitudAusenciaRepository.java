package com.rrhh.ausencias.repository;

import com.rrhh.ausencias.model.EstadoSolicitudAusencia;
import com.rrhh.ausencias.model.SolicitudAusencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudAusenciaRepository extends JpaRepository<SolicitudAusencia, String> {
    List<SolicitudAusencia> findByTenantIdOrderByCreadoEnDesc(String tenantId);

    List<SolicitudAusencia> findByTrabajadorIdOrderByCreadoEnDesc(String trabajadorId);

    Optional<SolicitudAusencia> findByIdAndTenantId(String id, String tenantId);

    List<SolicitudAusencia> findByTrabajadorIdAndEstadoOrderByCreadoEnDesc(
            String trabajadorId,
            EstadoSolicitudAusencia estado
    );

    List<SolicitudAusencia> findByTenantIdAndEstadoOrderByCreadoEnDesc(
            String tenantId,
            EstadoSolicitudAusencia estado
    );
}
