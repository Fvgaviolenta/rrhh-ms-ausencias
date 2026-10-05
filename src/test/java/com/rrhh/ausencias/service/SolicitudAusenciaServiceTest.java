package com.rrhh.ausencias.service;

import com.rrhh.ausencias.dto.request.CrearSolicitudAusenciaRequest;
import com.rrhh.ausencias.dto.request.EvaluarSolicitudAusenciaRequest;
import com.rrhh.ausencias.exception.DomainException;
import com.rrhh.ausencias.model.EstadoSolicitudAusencia;
import com.rrhh.ausencias.model.SolicitudAusencia;
import com.rrhh.ausencias.repository.SolicitudAusenciaRepository;
import com.rrhh.ausencias.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudAusenciaServiceTest {

    @Mock
    private SolicitudAusenciaRepository solicitudAusenciaRepository;
    @Mock
    private TenantContext tenantContext;
    @InjectMocks
    private SolicitudAusenciaService service;

    @Test
    void laSolicitudNacePendiente() {
        when(tenantContext.require()).thenReturn(trabajador("trab-1"));
        when(solicitudAusenciaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = service.crear(new CrearSolicitudAusenciaRequest(
                LocalDate.of(2026, 9, 22), "permiso", "Control médico"
        ));

        assertEquals(EstadoSolicitudAusencia.PENDIENTE, respuesta.estado());
        ArgumentCaptor<SolicitudAusencia> captor = ArgumentCaptor.forClass(SolicitudAusencia.class);
        verify(solicitudAusenciaRepository).save(captor.capture());
        assertEquals(LocalDate.of(2026, 9, 22), captor.getValue().getFechaInicio());
        assertEquals(captor.getValue().getFechaInicio(), captor.getValue().getFechaFin());
    }

    @Test
    void noReevaluaUnaSolicitudCerrada() {
        when(tenantContext.require()).thenReturn(admin());
        SolicitudAusencia solicitud = pendiente();
        solicitud.setEstado(EstadoSolicitudAusencia.APROBADO);
        when(solicitudAusenciaRepository.findByIdAndTenantId("sol-1", "tenant-1")).thenReturn(Optional.of(solicitud));

        DomainException error = assertThrows(DomainException.class, () -> service.evaluar(
                "sol-1", new EvaluarSolicitudAusenciaRequest(EstadoSolicitudAusencia.RECHAZADO, "tarde")
        ));

        assertEquals(400, error.getCodigo());
    }

    @Test
    void elTrabajadorNoEvaluaLaPropia() {
        when(tenantContext.require()).thenReturn(new TenantContext.AuthenticatedUser(
                "tenant-1", "user-jefatura", "jefe@rrhh.local", "Jefatura", "trab-1", "sub"
        ));
        when(solicitudAusenciaRepository.findByIdAndTenantId("sol-1", "tenant-1")).thenReturn(Optional.of(pendiente()));

        DomainException error = assertThrows(DomainException.class, () -> service.evaluar(
                "sol-1", new EvaluarSolicitudAusenciaRequest(EstadoSolicitudAusencia.APROBADO, null)
        ));

        assertEquals(403, error.getCodigo());
    }

    private static TenantContext.AuthenticatedUser trabajador(String trabajadorId) {
        return new TenantContext.AuthenticatedUser(
                "tenant-1", "user-1", "ana@rrhh.local", "Trabajador", trabajadorId, "sub"
        );
    }

    private static TenantContext.AuthenticatedUser admin() {
        return new TenantContext.AuthenticatedUser(
                "tenant-1", "user-admin", "admin.demo@rrhh.local", "Admin de RRHH", null, "sub"
        );
    }

    private static SolicitudAusencia pendiente() {
        SolicitudAusencia solicitud = new SolicitudAusencia();
        solicitud.setId("sol-1");
        solicitud.setTenantId("tenant-1");
        solicitud.setTrabajadorId("trab-1");
        solicitud.setTipo("permiso");
        solicitud.setFechaInicio(LocalDate.of(2026, 9, 22));
        solicitud.setFechaFin(LocalDate.of(2026, 9, 22));
        solicitud.setEstado(EstadoSolicitudAusencia.PENDIENTE);
        solicitud.setMotivo("Control médico");
        return solicitud;
    }
}
