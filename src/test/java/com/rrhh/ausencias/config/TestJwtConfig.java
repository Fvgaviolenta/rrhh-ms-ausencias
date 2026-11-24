package com.rrhh.ausencias.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

@TestConfiguration
public class TestJwtConfig {
    /** Empresa del token "admin-suspendida"; TenantSuspendidoIT la crea en identity_tenant y la suspende. */
    public static final String TENANT_SUSPENDIBLE = "99999999-9999-9999-9999-999999999999";
    /** custom:user_id del token "operador-grupo"; PlataformaAlcanceIT lo asigna a su grupo. */
    public static final String OPERADOR_GRUPO_ID = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";
    /** Subject del token "operador-grupo-sin-claim" (sin custom:user_id): su fila se busca por cognito_sub. */
    public static final String OPERADOR_SIN_CLAIM_SUB = "op-sin-claim";

    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return token -> switch (token) {
            case "otro-tenant" -> buildJwt("b", "admin.b@rrhh.local", "cccccccc-cccc-cccc-cccc-cccccccccccc", "Admin de RRHH", null);
            case "trabajador" -> buildJwt("t", "ana.perez@rrhh.local", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "Trabajador", "ffffffff-ffff-ffff-ffff-ffffffffffff");
            case "admin-suspendida" -> buildJwt("s", "admin.suspendida@rrhh.local", TENANT_SUSPENDIBLE, "Admin de RRHH", null);
            case "admin-plataforma" -> buildJwt("owner", "owner@gmail.com", null, "AdminPlataforma", null);
            case "admin-plataforma-con-tenant" -> buildJwt("owner", "owner@gmail.com", TENANT_SUSPENDIBLE, "AdminPlataforma", null);
            case "operador-grupo" -> conUserId(buildJwt("op", "operador.grupo@rrhh.local", null, "OperadorSaaS", null), OPERADOR_GRUPO_ID);
            case "operador-sin-grupo" -> conUserId(buildJwt("op-solo", "operador.solo@rrhh.local", null, "OperadorSaaS", null),
                    "dddddddd-0000-0000-0000-000000000001");
            case "operador-grupo-con-tenant" -> conUserId(buildJwt("op", "operador.grupo@rrhh.local", TENANT_SUSPENDIBLE, "OperadorSaaS", null),
                    OPERADOR_GRUPO_ID);
            case "operador-grupo-sin-claim" -> buildJwt(OPERADOR_SIN_CLAIM_SUB, "operador.grupo@rrhh.local", null, "OperadorSaaS", null);
            default -> buildJwt("a", "admin.demo@rrhh.local", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "Admin de RRHH", null);
        };
    }

    private static Jwt buildJwt(String subject, String email, String tenantId, String role, String trabajadorId) {
        var builder = Jwt.withTokenValue("test")
                .header("alg", "none")
                .subject(subject)
                .claim("email", email)
                .claim("custom:role", role)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        if (tenantId != null) {
            builder.claim("custom:tenant_id", tenantId);
        }
        if (trabajadorId != null) {
            builder.claim("custom:trabajador_id", trabajadorId);
        }
        return builder.build();
    }

    private static Jwt conUserId(Jwt jwt, String userId) {
        return Jwt.withTokenValue(jwt.getTokenValue())
                .headers(h -> h.putAll(jwt.getHeaders()))
                .claims(c -> {
                    c.putAll(jwt.getClaims());
                    c.put("custom:user_id", userId);
                })
                .build();
    }
}
