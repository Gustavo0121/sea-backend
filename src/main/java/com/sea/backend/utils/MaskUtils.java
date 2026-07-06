package com.sea.backend.utils;

import com.sea.backend.entity.TipoTelefone;

public final class MaskUtils {

    private static final int TAMANHO_CPF = 11;
    private static final int TAMANHO_CEP = 8;
    private static final int TAMANHO_CELULAR = 11;
    private static final int TAMANHO_FIXO = 10;

    private MaskUtils() {
    }

    public static String maskCpf(String cpf) {
        String digits = DigitExtractor.onlyDigits(cpf);
        if (digits.length() != TAMANHO_CPF) {
            return cpf;
        }
        return digits.substring(0, 3) + ".***.***-" + digits.substring(9);
    }

    public static String maskCep(String cep) {
        String digits = DigitExtractor.onlyDigits(cep);
        if (digits.length() != TAMANHO_CEP) {
            return cep;
        }
        return digits.substring(0, 5) + "-" + digits.substring(5);
    }

    public static String maskTelefone(TipoTelefone tipo, String numero) {
        String digits = DigitExtractor.onlyDigits(numero);
        int tamanhoEsperado = tipo == TipoTelefone.CELULAR ? TAMANHO_CELULAR : TAMANHO_FIXO;
        if (digits.length() != tamanhoEsperado) {
            return numero;
        }
        String ddd = digits.substring(0, 2);
        String assinante = digits.substring(2);
        int corte = assinante.length() - 4;
        return "(" + ddd + ") " + assinante.substring(0, corte) + "-" + assinante.substring(corte);
    }
}
