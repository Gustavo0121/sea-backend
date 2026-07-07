package com.sea.backend.dto;

import com.sea.backend.entity.TipoTelefone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void deveAceitarClienteValido() {
        Set<ConstraintViolation<ClienteRequestDTO>> violacoes = validator.validate(clienteValido());

        assertThat(violacoes).isEmpty();
    }

    @Test
    void deveRejeitarNomeComMenosDeTresCaracteres() {
        ClienteRequestDTO dto = clienteValido();
        dto.setNome("Jo");

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void deveRejeitarNomeComCaracteresInvalidos() {
        ClienteRequestDTO dto = clienteValido();
        dto.setNome("João <b>Silva</b>");

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void deveRejeitarCpfComDigitoVerificadorInvalido() {
        ClienteRequestDTO dto = clienteValido();
        dto.setCpf("111.444.777-36");

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void deveRejeitarEnderecoAusente() {
        ClienteRequestDTO dto = clienteValido();
        dto.setEndereco(null);

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void deveRejeitarListaDeTelefonesVazia() {
        ClienteRequestDTO dto = clienteValido();
        dto.setTelefones(Collections.emptyList());

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void deveRejeitarListaDeEmailsVazia() {
        ClienteRequestDTO dto = clienteValido();
        dto.setEmails(Collections.emptyList());

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void deveRejeitarTelefoneCelularComQuantidadeErradaDeDigitos() {
        ClienteRequestDTO dto = clienteValido();
        TelefoneRequestDTO telefone = new TelefoneRequestDTO();
        telefone.setTipo(TipoTelefone.CELULAR);
        telefone.setNumero("1234567");
        dto.setTelefones(Arrays.asList(telefone));

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void deveRejeitarEmailComFormatoInvalido() {
        ClienteRequestDTO dto = clienteValido();
        EmailRequestDTO email = new EmailRequestDTO();
        email.setEndereco("nao-e-um-email");
        dto.setEmails(Arrays.asList(email));

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    private ClienteRequestDTO clienteValido() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNome("João da Silva");
        dto.setCpf("111.444.777-35");
        dto.setEndereco(enderecoValido());
        dto.setTelefones(Arrays.asList(telefoneValido()));
        dto.setEmails(Arrays.asList(emailValido()));
        return dto;
    }

    private EnderecoRequestDTO enderecoValido() {
        EnderecoRequestDTO endereco = new EnderecoRequestDTO();
        endereco.setCep("01310-100");
        endereco.setLogradouro("Av. Paulista");
        endereco.setBairro("Bela Vista");
        endereco.setCidade("São Paulo");
        endereco.setUf("SP");
        return endereco;
    }

    private TelefoneRequestDTO telefoneValido() {
        TelefoneRequestDTO telefone = new TelefoneRequestDTO();
        telefone.setTipo(TipoTelefone.CELULAR);
        telefone.setNumero("11987654321");
        return telefone;
    }

    private EmailRequestDTO emailValido() {
        EmailRequestDTO email = new EmailRequestDTO();
        email.setEndereco("joao.silva@example.com");
        return email;
    }
}
