package com.sea.backend.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextSanitizerTest {

    @Test
    void deveColapsarEspacosDuplicados() {
        assertThat(TextSanitizer.collapseSpaces("João   da   Silva")).isEqualTo("João da Silva");
    }

    @Test
    void deveRemoverEspacosNasBordas() {
        assertThat(TextSanitizer.collapseSpaces("  João Silva  ")).isEqualTo("João Silva");
    }

    @Test
    void deveRemoverCaracteresPerigososMantendoTextoLegitimo() {
        assertThat(TextSanitizer.sanitize("Av. Brasil <script>alert(1)</script> nº 123"))
                .isEqualTo("Av. Brasil scriptalert(1)/script nº 123");
    }

    @Test
    void deveRetornarNuloQuandoEntradaForNula() {
        assertThat(TextSanitizer.sanitize(null)).isNull();
        assertThat(TextSanitizer.collapseSpaces(null)).isNull();
    }
}
