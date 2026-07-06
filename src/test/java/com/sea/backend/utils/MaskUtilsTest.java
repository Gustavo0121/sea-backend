package com.sea.backend.utils;

import com.sea.backend.entity.TipoTelefone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskUtilsTest {

    @Test
    void deveMascararCpfMantendoPrimeirosTresEUltimosDoisDigitos() {
        assertThat(MaskUtils.maskCpf("11144477735")).isEqualTo("111.***.***-35");
    }

    @Test
    void deveFormatarCepComTraco() {
        assertThat(MaskUtils.maskCep("01310100")).isEqualTo("01310-100");
    }

    @Test
    void deveMascararCelularComNoveDigitosNoAssinante() {
        assertThat(MaskUtils.maskTelefone(TipoTelefone.CELULAR, "11987654321")).isEqualTo("(11) 98765-4321");
    }

    @Test
    void deveMascararFixoComOitoDigitosNoAssinante() {
        assertThat(MaskUtils.maskTelefone(TipoTelefone.RESIDENCIAL, "1123456789")).isEqualTo("(11) 2345-6789");
    }

    @Test
    void deveRetornarValorOriginalQuandoTamanhoForInvalido() {
        assertThat(MaskUtils.maskCpf("123")).isEqualTo("123");
        assertThat(MaskUtils.maskCep("123")).isEqualTo("123");
        assertThat(MaskUtils.maskTelefone(TipoTelefone.CELULAR, "123")).isEqualTo("123");
    }
}
