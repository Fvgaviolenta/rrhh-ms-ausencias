package com.rrhh.ausencias.controller;

import com.rrhh.ausencias.AusenciasApplication;
import com.rrhh.ausencias.config.TestJwtConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AusenciasApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwtConfig.class)
class SolicitudAusenciaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminListaSolicitudesDelTenant() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes-ausencia").header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.length()").value(1))
                .andExpect(jsonPath("$.datos[0].tenant_id").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(jsonPath("$.datos[0].estado").value("PENDIENTE"));
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
                .andExpect(jsonPath("$.datos.status").value("UP"));
    }
}
