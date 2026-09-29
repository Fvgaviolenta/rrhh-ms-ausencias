package com.rrhh.ausencias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.ausencias.AusenciasApplication;
import com.rrhh.ausencias.config.TestJwtConfig;
import com.rrhh.ausencias.dto.request.RechazarAusenciaRequest;
import com.rrhh.ausencias.dto.request.SolicitarAusenciaRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AusenciasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwtConfig.class)
class SolicitudAusenciaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminListaSolicitudesDelTenant() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes-ausencia").header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").isArray());
    }

    @Test
    void listarSolicitudesPorTrabajador() throws Exception {
        mockMvc.perform(get("/api/v1/ausencias/trabajador/test-trabajador-id")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").isArray());
    }

    @Test
    void obtenerSolicitudPorId() throws Exception {
        mockMvc.perform(get("/api/v1/ausencias/test-solicitud-id")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").exists());
    }

    @Test
    void misSolicitudes() throws Exception {
        mockMvc.perform(get("/api/v1/ausencias/mis-solicitudes")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").isArray());
    }

    @Test
    void solicitarAusencia() throws Exception {
        SolicitarAusenciaRequest request = new SolicitarAusenciaRequest(
                "test-trabajador-id",
                "Vacaciones",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 15),
                "Vacaciones anuales"
        );

        mockMvc.perform(post("/api/v1/ausencias/solicitar")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Solicitud de ausencia creada"));
    }

    @Test
    void solicitarAusenciaConFechaInvalida() throws Exception {
        SolicitarAusenciaRequest request = new SolicitarAusenciaRequest(
                "test-trabajador-id",
                "Vacaciones",
                LocalDate.of(2024, 2, 15),
                LocalDate.of(2024, 2, 1), // Fecha fin antes de inicio
                "Vacaciones anuales"
        );

        mockMvc.perform(post("/api/v1/ausencias/solicitar")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aprobarSolicitud() throws Exception {
        mockMvc.perform(patch("/api/v1/ausencias/test-solicitud-id/aprobar")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Solicitud aprobada"));
    }

    @Test
    void rechazarSolicitud() throws Exception {
        RechazarAusenciaRequest request = new RechazarAusenciaRequest("No cumple requisitos");

        mockMvc.perform(patch("/api/v1/ausencias/test-solicitud-id/rechazar")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Solicitud rechazada"));
    }

    @Test
    void otroTenantNoVeSolicitudes() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes-ausencia").header("Authorization", "Bearer otro-tenant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.length()").value(0));
    }

    @Test
    void trabajadorNoListaSolicitudes() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes-ausencia").header("Authorization", "Bearer trabajador"))
                .andExpect(status().isForbidden());
    }

    @Test
    void statusEndpointResponde() throws Exception {
        mockMvc.perform(get("/api/v1/ausencias/status").header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.service").value("rrhh-ausencias"))
                .andExpect(jsonPath("$.datos.status").value("OPERATIVO"));
    }

    @Test
    void solicitarAusenciaSinToken() throws Exception {
        SolicitarAusenciaRequest request = new SolicitarAusenciaRequest(
                "test-trabajador-id",
                "Vacaciones",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 15),
                "Vacaciones anuales"
        );

        mockMvc.perform(post("/api/v1/ausencias/solicitar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aprobarSolicitudSinPermisos() throws Exception {
        mockMvc.perform(patch("/api/v1/ausencias/test-solicitud-id/aprobar")
                        .header("Authorization", "Bearer trabajador"))
                .andExpect(status().isForbidden());
    }
}
