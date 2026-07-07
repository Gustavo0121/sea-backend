package com.sea.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Exercises the real Spring-managed ViaCepService bean (proxied by @Cacheable) to confirm our
 * cache wiring works end to end: a second lookup for the same CEP, even in a different textual
 * format, must not trigger a second HTTP call.
 */
@SpringBootTest
@ActiveProfiles("test")
class ViaCepServiceCacheTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ViaCepService viaCepService;

    @Test
    void deveReutilizarRespostaDoCacheParaOMesmoCep() {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(requestTo("https://viacep.com.br/ws/20040020/json/"))
                .andRespond(withSuccess(
                        "{\"logradouro\":\"Avenida Rio Branco\",\"bairro\":\"Centro\",\"localidade\":\"Rio de Janeiro\",\"uf\":\"RJ\"}",
                        MediaType.APPLICATION_JSON));

        assertThat(viaCepService.consultar("20040-020").getLogradouro()).isEqualTo("Avenida Rio Branco");
        assertThat(viaCepService.consultar("20040020").getLogradouro()).isEqualTo("Avenida Rio Branco");

        mockServer.verify();
    }
}
