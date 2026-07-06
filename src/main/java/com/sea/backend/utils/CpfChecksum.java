package com.sea.backend.utils;

/**
 * Implements the standard Brazilian CPF check-digit algorithm (modulo 11 over the first 9,
 * then 10, digits). Sequences of a single repeated digit (e.g. "00000000000") pass the checksum
 * arithmetically but are not valid CPFs, so they are rejected explicitly.
 */
public final class CpfChecksum {

    private static final int TAMANHO_CPF = 11;

    private CpfChecksum() {
    }

    public static boolean isValid(String cpf) {
        String digits = DigitExtractor.onlyDigits(cpf);
        if (digits.length() != TAMANHO_CPF || todosDigitosIguais(digits)) {
            return false;
        }
        return calcularDigitoVerificador(digits, 9) == digitoEm(digits, 9)
                && calcularDigitoVerificador(digits, 10) == digitoEm(digits, 10);
    }

    private static boolean todosDigitosIguais(String digits) {
        for (int i = 1; i < digits.length(); i++) {
            if (digits.charAt(i) != digits.charAt(0)) {
                return false;
            }
        }
        return true;
    }

    private static int calcularDigitoVerificador(String digits, int quantidadeDigitos) {
        int soma = 0;
        int peso = quantidadeDigitos + 1;
        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += digitoEm(digits, i) * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int digitoEm(String digits, int index) {
        return digits.charAt(index) - '0';
    }
}
