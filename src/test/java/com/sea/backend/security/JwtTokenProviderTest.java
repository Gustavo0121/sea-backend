package com.sea.backend.security;

import com.sea.backend.entity.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-secret-key-with-at-least-32-bytes-long";

    @Test
    void deveGerarTokenValidoComLoginERole() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        String token = provider.generateToken("admin", Role.ADMIN);

        assertThat(provider.isValid(token)).isTrue();
        assertThat(provider.getLogin(token)).isEqualTo("admin");
        assertThat(provider.getRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void deveExpirarTokenAposTempoConfigurado() throws InterruptedException {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 1);

        String token = provider.generateToken("user", Role.USER);
        Thread.sleep(20);

        assertThat(provider.isValid(token)).isFalse();
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        String token = provider.generateToken("admin", Role.ADMIN);
        String adulterado = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThat(provider.isValid(adulterado)).isFalse();
    }

    @Test
    void deveRejeitarTokenAssinadoComOutroSegredo() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);
        JwtTokenProvider outroProvider = new JwtTokenProvider("outro-segredo-com-pelo-menos-32-bytes-de-tamanho", 60_000);

        String token = outroProvider.generateToken("admin", Role.ADMIN);

        assertThat(provider.isValid(token)).isFalse();
    }

    @Test
    void deveExporExpiracaoEmSegundos() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 900_000);

        assertThat(provider.getExpirationSeconds()).isEqualTo(900);
    }
}
