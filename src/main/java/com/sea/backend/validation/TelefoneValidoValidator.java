package com.sea.backend.validation;

import com.sea.backend.dto.TelefoneRequestDTO;
import com.sea.backend.entity.TipoTelefone;
import com.sea.backend.utils.DigitExtractor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TelefoneValidoValidator implements ConstraintValidator<TelefoneValido, TelefoneRequestDTO> {

    private static final int TAMANHO_CELULAR = 11;
    private static final int TAMANHO_FIXO = 10;

    @Override
    public boolean isValid(TelefoneRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getTipo() == null || dto.getNumero() == null) {
            return true;
        }
        String digits = DigitExtractor.onlyDigits(dto.getNumero());
        int tamanhoEsperado = dto.getTipo() == TipoTelefone.CELULAR ? TAMANHO_CELULAR : TAMANHO_FIXO;
        return digits.length() == tamanhoEsperado;
    }
}
