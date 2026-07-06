package com.sea.backend.service;

import com.sea.backend.dto.EnderecoResponseDTO;
import com.sea.backend.exception.CepNaoEncontradoException;
import com.sea.backend.exception.ViaCepIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ViaCepServiceTest {

    private MockRestServiceServer mockServer;
    private ViaCepService viaCepService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        viaCepService = new ViaCepService(restTemplate);
    }

    @Test
    void deveRetornarEnderecoQuandoCepExiste() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/01310100/json/"))
                .andRespond(withSuccess(
                        "{\"cep\":\"01310-100\",\"logradouro\":\"Avenida Paulista\",\"bairro\":\"Bela Vista\","
                                + "\"localidade\":\"São Paulo\",\"uf\":\"SP\"}",
                        MediaType.APPLICATION_JSON));

        EnderecoResponseDTO endereco = viaCepService.consultar("01310-100");

        assertThat(endereco.getCep()).isEqualTo("01310-100");
        assertThat(endereco.getLogradouro()).isEqualTo("Avenida Paulista");
        assertThat(endereco.getBairro()).isEqualTo("Bela Vista");
        assertThat(endereco.getCidade()).isEqualTo("São Paulo");
        assertThat(endereco.getUf()).isEqualTo("SP");
        assertThat(endereco.getComplemento()).isNull();
        mockServer.verify();
    }

    @Test
    void deveNormalizarCepComOuSemMascaraAntesDeChamarAApi() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/01310100/json/"))
                .andRespond(withSuccess("{\"logradouro\":\"Avenida Paulista\",\"uf\":\"SP\"}", MediaType.APPLICATION_JSON));

        viaCepService.consultar("01310100");

        mockServer.verify();
    }

    @Test
    void deveLancarExcecaoQuandoCepNaoExiste() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/00000000/json/"))
                .andRespond(withSuccess("{\"erro\":true}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> viaCepService.consultar("00000000"))
                .isInstanceOf(CepNaoEncontradoException.class);
        mockServer.verify();
    }

    @Test
    void deveLancarExcecaoQuandoServicoIndisponivel() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/01310100/json/"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> viaCepService.consultar("01310-100"))
                .isInstanceOf(ViaCepIndisponivelException.class);
        mockServer.verify();
    }
}
