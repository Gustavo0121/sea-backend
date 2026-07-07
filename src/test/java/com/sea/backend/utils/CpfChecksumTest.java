package com.sea.backend.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfChecksumTest {

    @Test
    void deveAceitarCpfValidoSemMascara() {
        assertThat(CpfChecksum.isValid("11144477735")).isTrue();
    }

    @Test
    void deveAceitarCpfValidoComMascara() {
        assertThat(CpfChecksum.isValid("111.444.777-35")).isTrue();
    }

    @Test
    void deveRejeitarDigitoVerificadorIncorreto() {
        assertThat(CpfChecksum.isValid("11144477736")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000", "11111111111", "99999999999"})
    void deveRejeitarSequenciasDeDigitosRepetidos(String cpf) {
        assertThat(CpfChecksum.isValid(cpf)).isFalse();
    }

    @Test
    void deveRejeitarTamanhoDiferenteDeOnzeDigitos() {
        assertThat(CpfChecksum.isValid("123456789")).isFalse();
    }

    @Test
    void deveRejeitarNulo() {
        assertThat(CpfChecksum.isValid(null)).isFalse();
    }
}
