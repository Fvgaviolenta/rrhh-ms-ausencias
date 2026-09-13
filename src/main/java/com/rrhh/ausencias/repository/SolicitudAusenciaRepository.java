package com.rrhh.ausencias.repository;

import com.rrhh.ausencias.model.SolicitudAusencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudAusenciaRepository extends JpaRepository<SolicitudAusencia, String> {
    List<SolicitudAusencia> findByTenantIdOrderByCreadoEnDesc(String tenantId);
}
