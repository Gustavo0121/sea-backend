package com.sea.backend.dto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

class EnderecoRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void deveAceitarCepComOuSemMascara() {
        assertThat(validator.validate(endereco("01310-100"))).isEmpty();
        assertThat(validator.validate(endereco("01310100"))).isEmpty();
    }

    @Test
    void deveRejeitarCepComQuantidadeErradaDeDigitos() {
        assertThat(validator.validate(endereco("123"))).isNotEmpty();
    }

    @Test
    void deveRejeitarUfComMaisDeDuasLetras() {
        EnderecoRequestDTO dto = endereco("01310-100");
        dto.setUf("SPX");

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void devePermitirComplementoAusente() {
        EnderecoRequestDTO dto = endereco("01310-100");
        dto.setComplemento(null);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void deveRejeitarLogradouroEmBranco() {
        EnderecoRequestDTO dto = endereco("01310-100");
        dto.setLogradouro(" ");

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    private EnderecoRequestDTO endereco(String cep) {
        EnderecoRequestDTO dto = new EnderecoRequestDTO();
        dto.setCep(cep);
        dto.setLogradouro("Av. Paulista");
        dto.setBairro("Bela Vista");
        dto.setCidade("São Paulo");
        dto.setUf("SP");
        return dto;
    }
}
