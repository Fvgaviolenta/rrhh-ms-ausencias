package com.rrhh.ausencias.dto.response;

public record ServiceStatusResponse(
        String service,
        String version,
        String status
) {}
