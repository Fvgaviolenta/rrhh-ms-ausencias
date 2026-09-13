CREATE TABLE ausencias_solicitud_ausencia (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id CHAR(36) NOT NULL,
    trabajador_id CHAR(36) NOT NULL,
    tipo VARCHAR(60) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    motivo VARCHAR(500) NULL,
    creado_en DATETIME NOT NULL
);

CREATE INDEX idx_solicitud_tenant ON ausencias_solicitud_ausencia (tenant_id);
CREATE INDEX idx_solicitud_trabajador ON ausencias_solicitud_ausencia (tenant_id, trabajador_id);
