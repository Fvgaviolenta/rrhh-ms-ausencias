INSERT INTO ausencias_solicitud_ausencia (
    id, tenant_id, trabajador_id, tipo, fecha_inicio, fecha_fin, estado, motivo, creado_en
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'ffffffff-ffff-ffff-ffff-ffffffffffff',
    'VACACIONES',
    '2026-03-01',
    '2026-03-05',
    'PENDIENTE',
    'Vacaciones de verano',
    CURRENT_TIMESTAMP
);
