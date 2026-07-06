package com.sea.backend.security;

import com.sea.backend.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /clientes only exists as an authorization contract until Phase 5 implements the controller;
 * a 404 here proves the request passed security and reached (the still-missing) dispatch,
 * which is enough to confirm the role rule allowed it through.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void deveBloquearRequisicaoSemTokenComo401() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveBloquearTokenInvalidoComo401() throws Exception {
        mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveDeixarAdminEUserLeremClientes() throws Exception {
        mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBloquearUsuarioPadraoDeEscreverClientesComo403() throws Exception {
        String userToken = bearer(Role.USER);

        mockMvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/clientes/1").header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveDeixarAdminEscreverClientes() throws Exception {
        mockMvc.perform(post("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExporHeadersDeSegurancaNaResposta() throws Exception {
        mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    private String bearer(Role role) {
        return "Bearer " + jwtTokenProvider.generateToken(role == Role.ADMIN ? "admin" : "user", role);
    }
}
