package com.sea.backend.exception;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sea.backend.dto.ClienteRequestDTO;
import com.sea.backend.dto.EmailRequestDTO;
import com.sea.backend.dto.EnderecoRequestDTO;
import com.sea.backend.dto.LoginRequest;
import com.sea.backend.dto.TelefoneRequestDTO;
import com.sea.backend.entity.TipoTelefone;
import com.sea.backend.service.ClienteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Verifica a regra da Fase 6: senha, token JWT e CPF completo nunca podem aparecer em nenhuma
 * linha de log, mesmo em fluxos que os manipulam diretamente (login com/sem sucesso, cadastro de
 * cliente). @Transactional garante rollback do cliente cadastrado ao final de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoggingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteService clienteService;

    private ch.qos.logback.classic.Logger rootLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        rootLogger.detachAppender(appender);
    }

    @Test
    void naoDeveRegistrarSenhaEmTentativaDeLoginComFalha() throws Exception {
        String senha = "senha-secreta-de-teste";

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest("admin", senha))));

        assertThat(logsContendo(senha)).isEmpty();
    }

    @Test
    void naoDeveRegistrarSenhaNemTokenEmLoginComSucesso() throws Exception {
        String senha = "123qwe!@#";

        String corpo = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest("admin", senha))))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(corpo).get("token").asText();

        assertThat(logsContendo(senha)).isEmpty();
        assertThat(logsContendo(token)).isEmpty();
    }

    @Test
    void naoDeveRegistrarCpfCompletoAoCadastrarCliente() {
        String cpf = "52998224725";

        clienteService.cadastrar(clienteRequestValido(cpf));

        assertThat(logsContendo(cpf)).isEmpty();
    }

    private List<String> logsContendo(String trecho) {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(mensagem -> mensagem.contains(trecho))
                .collect(Collectors.toList());
    }

    private LoginRequest loginRequest(String login, String senha) {
        LoginRequest request = new LoginRequest();
        request.setLogin(login);
        request.setSenha(senha);
        return request;
    }

    private ClienteRequestDTO clienteRequestValido(String cpf) {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNome("Maria Souza");
        dto.setCpf(cpf);
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
        email.setEndereco("maria.souza@example.com");
        return email;
    }
}
