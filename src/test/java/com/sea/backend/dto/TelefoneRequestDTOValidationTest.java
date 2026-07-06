package com.sea.backend.dto;

import com.sea.backend.entity.TipoTelefone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

class TelefoneRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void deveAceitarCelularComOnzeDigitos() {
        assertThat(validator.validate(telefone(TipoTelefone.CELULAR, "11987654321"))).isEmpty();
    }

    @Test
    void deveAceitarFixoComDezDigitos() {
        assertThat(validator.validate(telefone(TipoTelefone.RESIDENCIAL, "1123456789"))).isEmpty();
        assertThat(validator.validate(telefone(TipoTelefone.COMERCIAL, "1123456789"))).isEmpty();
    }

    @Test
    void deveAceitarNumeroComMascara() {
        assertThat(validator.validate(telefone(TipoTelefone.CELULAR, "(11) 98765-4321"))).isEmpty();
    }

    @Test
    void deveRejeitarCelularComDezDigitos() {
        assertThat(validator.validate(telefone(TipoTelefone.CELULAR, "1123456789"))).isNotEmpty();
    }

    @Test
    void deveRejeitarFixoComOnzeDigitos() {
        assertThat(validator.validate(telefone(TipoTelefone.RESIDENCIAL, "11987654321"))).isNotEmpty();
    }

    @Test
    void deveRejeitarTipoAusente() {
        TelefoneRequestDTO dto = telefone(TipoTelefone.CELULAR, "11987654321");
        dto.setTipo(null);

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    private TelefoneRequestDTO telefone(TipoTelefone tipo, String numero) {
        TelefoneRequestDTO dto = new TelefoneRequestDTO();
        dto.setTipo(tipo);
        dto.setNumero(numero);
        return dto;
    }
}
