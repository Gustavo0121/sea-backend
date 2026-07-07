package com.sea.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sea.backend.dto.ClienteRequestDTO;
import com.sea.backend.dto.ClienteResponseDTO;
import com.sea.backend.dto.EmailRequestDTO;
import com.sea.backend.dto.EnderecoRequestDTO;
import com.sea.backend.dto.TelefoneRequestDTO;
import com.sea.backend.entity.Role;
import com.sea.backend.entity.TipoTelefone;
import com.sea.backend.exception.ClienteNaoEncontradoException;
import com.sea.backend.exception.CpfDuplicadoException;
import com.sea.backend.security.JwtTokenProvider;
import com.sea.backend.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ClienteService clienteService;

    @Test
    void deveListarClientesParaAdminEUser() throws Exception {
        Page<ClienteResponseDTO> pagina = new PageImpl<>(Collections.singletonList(clienteResponseDTO()));
        given(clienteService.listar(any(), any(), any())).willReturn(pagina);

        mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("João da Silva"));

        mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isOk());
    }

    @Test
    void deveBuscarPorIdQuandoExiste() throws Exception {
        given(clienteService.buscarPorId(1L)).willReturn(clienteResponseDTO());

        mockMvc.perform(get("/clientes/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("111.***.***-35"));
    }

    @Test
    void deveRetornar404QuandoClienteNaoEncontrado() throws Exception {
        given(clienteService.buscarPorId(99L)).willThrow(new ClienteNaoEncontradoException(99L));

        mockMvc.perform(get("/clientes/99").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCadastrarClienteComoAdmin() throws Exception {
        given(clienteService.cadastrar(any())).willReturn(clienteResponseDTO());

        mockMvc.perform(post("/clientes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João da Silva"));
    }

    @Test
    void deveRejeitarCadastroComoUsuarioPadrao() throws Exception {
        mockMvc.perform(post("/clientes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.USER))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarCadastroComDadosInvalidos() throws Exception {
        ClienteRequestDTO invalido = new ClienteRequestDTO();

        mockMvc.perform(post("/clientes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409QuandoCpfDuplicado() throws Exception {
        given(clienteService.cadastrar(any())).willThrow(new CpfDuplicadoException());

        mockMvc.perform(post("/clientes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isConflict());
    }

    @Test
    void deveAtualizarClienteComoAdmin() throws Exception {
        given(clienteService.atualizar(anyLong(), any())).willReturn(clienteResponseDTO());

        mockMvc.perform(put("/clientes/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isOk());
    }

    @Test
    void deveRejeitarAtualizacaoComoUsuarioPadrao() throws Exception {
        mockMvc.perform(put("/clientes/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.USER))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(clienteRequestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveExcluirClienteComoAdmin() throws Exception {
        mockMvc.perform(delete("/clientes/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRejeitarExclusaoComoUsuarioPadrao() throws Exception {
        mockMvc.perform(delete("/clientes/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearRequisicaoSemToken() throws Exception {
        mockMvc.perform(get("/clientes")).andExpect(status().isUnauthorized());
    }

    private ClienteRequestDTO clienteRequestValido() {
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

    private ClienteResponseDTO clienteResponseDTO() {
        return new ClienteResponseDTO(1L, "João da Silva", "111.***.***-35", null, null, null);
    }

    private String bearer(Role role) {
        return "Bearer " + jwtTokenProvider.generateToken(role == Role.ADMIN ? "admin" : "user", role);
    }
}
