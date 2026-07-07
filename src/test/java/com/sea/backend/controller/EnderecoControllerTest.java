package com.sea.backend.controller;

import com.sea.backend.dto.EnderecoResponseDTO;
import com.sea.backend.entity.Role;
import com.sea.backend.exception.CepNaoEncontradoException;
import com.sea.backend.exception.ViaCepIndisponivelException;
import com.sea.backend.security.JwtTokenProvider;
import com.sea.backend.service.ViaCepService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnderecoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ViaCepService viaCepService;

    @Test
    void deveRetornarEnderecoParaAdminAutenticado() throws Exception {
        given(viaCepService.consultar(anyString())).willReturn(
                new EnderecoResponseDTO("01310-100", "Avenida Paulista", "Bela Vista", "São Paulo", "SP", null));

        mockMvc.perform(get("/enderecos/01310-100").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logradouro").value("Avenida Paulista"))
                .andExpect(jsonPath("$.uf").value("SP"));
    }

    @Test
    void deveRetornar404QuandoCepNaoEncontrado() throws Exception {
        given(viaCepService.consultar(anyString())).willThrow(new CepNaoEncontradoException("00000000"));

        mockMvc.perform(get("/enderecos/00000000").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar503QuandoServicoIndisponivel() throws Exception {
        given(viaCepService.consultar(anyString())).willThrow(new ViaCepIndisponivelException(new RuntimeException("timeout")));

        mockMvc.perform(get("/enderecos/01310-100").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void deveRejeitarCepComFormatoInvalido() throws Exception {
        mockMvc.perform(get("/enderecos/123").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBloquearUsuarioPadraoComo403() throws Exception {
        mockMvc.perform(get("/enderecos/01310-100").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearRequisicaoSemTokenComo401() throws Exception {
        mockMvc.perform(get("/enderecos/01310-100"))
                .andExpect(status().isUnauthorized());
    }

    private String bearer(Role role) {
        return "Bearer " + jwtTokenProvider.generateToken(role == Role.ADMIN ? "admin" : "user", role);
    }
}
