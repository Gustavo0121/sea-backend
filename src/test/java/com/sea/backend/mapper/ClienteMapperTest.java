package com.sea.backend.mapper;

import com.sea.backend.dto.ClienteRequestDTO;
import com.sea.backend.dto.ClienteResponseDTO;
import com.sea.backend.dto.EmailRequestDTO;
import com.sea.backend.dto.EnderecoRequestDTO;
import com.sea.backend.dto.TelefoneRequestDTO;
import com.sea.backend.entity.Cliente;
import com.sea.backend.entity.Endereco;
import com.sea.backend.entity.TipoTelefone;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteMapperTest {

    private final EnderecoMapper enderecoMapper = new EnderecoMapper();
    private final TelefoneMapper telefoneMapper = new TelefoneMapper();
    private final EmailMapper emailMapper = new EmailMapper();
    private final ClienteMapper clienteMapper = new ClienteMapper(enderecoMapper, telefoneMapper, emailMapper);

    @Test
    void toEntityDevePersistirApenasDigitosENormalizarEspacos() {
        Cliente cliente = clienteMapper.toEntity(clienteRequestValido());

        assertThat(cliente.getNome()).isEqualTo("João da Silva");
        assertThat(cliente.getCpf()).isEqualTo("11144477735");
        assertThat(cliente.getEndereco().getCep()).isEqualTo("01310100");
        assertThat(cliente.getTelefones()).hasSize(1);
        assertThat(cliente.getTelefones().get(0).getNumero()).isEqualTo("11987654321");
        assertThat(cliente.getTelefones().get(0).getCliente()).isSameAs(cliente);
        assertThat(cliente.getEmails()).hasSize(1);
        assertThat(cliente.getEmails().get(0).getCliente()).isSameAs(cliente);
    }

    @Test
    void toResponseDTODeveMascararCpfCepETelefone() {
        Cliente cliente = clienteMapper.toEntity(clienteRequestValido());
        cliente.setId(1L);

        ClienteResponseDTO response = clienteMapper.toResponseDTO(cliente);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCpf()).isEqualTo("111.***.***-35");
        assertThat(response.getEndereco().getCep()).isEqualTo("01310-100");
        assertThat(response.getTelefones().get(0).getNumero()).isEqualTo("(11) 98765-4321");
        assertThat(response.getEmails().get(0).getEndereco()).isEqualTo("joao.silva@example.com");
    }

    @Test
    void atualizarEntityDeveMutarEnderecoExistenteESubstituirTelefonesEEmails() {
        Cliente cliente = clienteMapper.toEntity(clienteRequestValido());
        Endereco enderecoOriginal = cliente.getEndereco();

        ClienteRequestDTO dtoAtualizado = clienteRequestValido();
        dtoAtualizado.setNome("Maria   Souza");
        dtoAtualizado.getEndereco().setCidade("Rio de Janeiro");
        TelefoneRequestDTO novoTelefone = new TelefoneRequestDTO();
        novoTelefone.setTipo(TipoTelefone.RESIDENCIAL);
        novoTelefone.setNumero("1133224455");
        dtoAtualizado.setTelefones(Arrays.asList(novoTelefone));
        EmailRequestDTO novoEmail = new EmailRequestDTO();
        novoEmail.setEndereco("maria.souza@example.com");
        dtoAtualizado.setEmails(Arrays.asList(novoEmail));

        clienteMapper.atualizarEntity(dtoAtualizado, cliente);

        assertThat(cliente.getNome()).isEqualTo("Maria Souza");
        assertThat(cliente.getEndereco())
                .as("endereço deve ser mutado, não substituído, para não órfãozar o registro persistido")
                .isSameAs(enderecoOriginal);
        assertThat(cliente.getEndereco().getCidade()).isEqualTo("Rio de Janeiro");
        assertThat(cliente.getTelefones()).hasSize(1);
        assertThat(cliente.getTelefones().get(0).getNumero()).isEqualTo("1133224455");
        assertThat(cliente.getTelefones().get(0).getCliente()).isSameAs(cliente);
        assertThat(cliente.getEmails()).hasSize(1);
        assertThat(cliente.getEmails().get(0).getEndereco()).isEqualTo("maria.souza@example.com");
    }

    private ClienteRequestDTO clienteRequestValido() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNome("João   da   Silva");
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
        endereco.setUf("sp");
        return endereco;
    }

    private TelefoneRequestDTO telefoneValido() {
        TelefoneRequestDTO telefone = new TelefoneRequestDTO();
        telefone.setTipo(TipoTelefone.CELULAR);
        telefone.setNumero("(11) 98765-4321");
        return telefone;
    }

    private EmailRequestDTO emailValido() {
        EmailRequestDTO email = new EmailRequestDTO();
        email.setEndereco("joao.silva@example.com");
        return email;
    }
}
