package com.sea.backend.repository;

import com.sea.backend.entity.Cliente;
import com.sea.backend.entity.Endereco;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void deveInformarExistenciaPorCpf() {
        clienteRepository.save(cliente("João da Silva", "11144477735"));

        assertThat(clienteRepository.existsByCpf("11144477735")).isTrue();
        assertThat(clienteRepository.existsByCpf("00000000000")).isFalse();
    }

    @Test
    void deveIgnorarOProprioIdAoChecarDuplicidadeDeCpf() {
        Cliente cliente = clienteRepository.save(cliente("João da Silva", "11144477735"));

        assertThat(clienteRepository.existsByCpfAndIdNot("11144477735", cliente.getId())).isFalse();
        assertThat(clienteRepository.existsByCpfAndIdNot("11144477735", cliente.getId() + 1)).isTrue();
    }

    @Test
    void deveFiltrarPorNomeParcialIgnorandoCaixa() {
        clienteRepository.save(cliente("João da Silva", "11144477735"));
        clienteRepository.save(cliente("Maria Souza", "52998224725"));

        Page<Cliente> pagina = clienteRepository.findByNomeContainingIgnoreCaseAndCpfContaining(
                "joão", "", PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).getNome()).isEqualTo("João da Silva");
    }

    @Test
    void deveFiltrarPorCpf() {
        clienteRepository.save(cliente("João da Silva", "11144477735"));
        clienteRepository.save(cliente("Maria Souza", "52998224725"));

        Page<Cliente> pagina = clienteRepository.findByNomeContainingIgnoreCaseAndCpfContaining(
                "", "529982", PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).getCpf()).isEqualTo("52998224725");
    }

    @Test
    void devePaginarResultados() {
        clienteRepository.save(cliente("Ana Lima", "11144477735"));
        clienteRepository.save(cliente("Bruno Costa", "52998224725"));
        clienteRepository.save(cliente("Carla Dias", "01377957019"));

        Page<Cliente> pagina = clienteRepository.findByNomeContainingIgnoreCaseAndCpfContaining(
                "", "", PageRequest.of(0, 2));

        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    private Cliente cliente(String nome, String cpf) {
        Endereco endereco = new Endereco("01310100", "Av. Paulista", "Bela Vista", "São Paulo", "SP", null);
        return new Cliente(nome, cpf, endereco);
    }
}
