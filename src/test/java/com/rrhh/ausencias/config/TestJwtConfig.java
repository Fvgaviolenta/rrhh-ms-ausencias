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
    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return token -> switch (token) {
            case "otro-tenant" -> buildJwt("b", "admin.b@rrhh.local", "cccccccc-cccc-cccc-cccc-cccccccccccc", "Admin de RRHH", null);
            case "trabajador" -> buildJwt("t", "ana.perez@rrhh.local", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "Trabajador", "ffffffff-ffff-ffff-ffff-ffffffffffff");
            default -> buildJwt("a", "admin.demo@rrhh.local", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "Admin de RRHH", null);
        };
    }

    private static Jwt buildJwt(String subject, String email, String tenantId, String role, String trabajadorId) {
        var builder = Jwt.withTokenValue("test")
                .header("alg", "none")
                .subject(subject)
                .claim("email", email)
                .claim("custom:tenant_id", tenantId)
                .claim("custom:role", role)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        if (trabajadorId != null) {
            builder.claim("custom:trabajador_id", trabajadorId);
        }
        return builder.build();
    }
}
