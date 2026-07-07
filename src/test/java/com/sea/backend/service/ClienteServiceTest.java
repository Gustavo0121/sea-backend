package com.sea.backend.service;

import com.sea.backend.dto.ClienteRequestDTO;
import com.sea.backend.dto.ClienteResponseDTO;
import com.sea.backend.entity.Cliente;
import com.sea.backend.entity.Endereco;
import com.sea.backend.exception.ClienteNaoEncontradoException;
import com.sea.backend.exception.CpfDuplicadoException;
import com.sea.backend.mapper.ClienteMapper;
import com.sea.backend.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepository, clienteMapper);
    }

    @Test
    void deveCadastrarQuandoCpfNaoEstaDuplicado() {
        ClienteRequestDTO dto = clienteRequestDTO("111.444.777-35");
        Cliente entidade = clienteEntity();
        given(clienteRepository.existsByCpf("11144477735")).willReturn(false);
        given(clienteMapper.toEntity(dto)).willReturn(entidade);
        given(clienteRepository.save(entidade)).willReturn(entidade);
        given(clienteMapper.toResponseDTO(entidade)).willReturn(clienteResponseDTO());

        ClienteResponseDTO resultado = clienteService.cadastrar(dto);

        assertThat(resultado).isNotNull();
        verify(clienteRepository).save(entidade);
    }

    @Test
    void deveRejeitarCadastroComCpfDuplicado() {
        ClienteRequestDTO dto = clienteRequestDTO("111.444.777-35");
        given(clienteRepository.existsByCpf("11144477735")).willReturn(true);

        assertThatThrownBy(() -> clienteService.cadastrar(dto)).isInstanceOf(CpfDuplicadoException.class);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveBuscarPorIdQuandoExiste() {
        Cliente entidade = clienteEntity();
        given(clienteRepository.findById(1L)).willReturn(Optional.of(entidade));
        given(clienteMapper.toResponseDTO(entidade)).willReturn(clienteResponseDTO());

        assertThat(clienteService.buscarPorId(1L)).isNotNull();
    }

    @Test
    void deveLancarExcecaoAoBuscarPorIdInexistente() {
        given(clienteRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(ClienteNaoEncontradoException.class);
    }

    @Test
    void deveAtualizarQuandoEncontradoENaoHaConflitoDeCpf() {
        ClienteRequestDTO dto = clienteRequestDTO("111.444.777-35");
        Cliente entidade = clienteEntity();
        given(clienteRepository.findById(1L)).willReturn(Optional.of(entidade));
        given(clienteRepository.existsByCpfAndIdNot("11144477735", 1L)).willReturn(false);
        given(clienteRepository.save(entidade)).willReturn(entidade);
        given(clienteMapper.toResponseDTO(entidade)).willReturn(clienteResponseDTO());

        clienteService.atualizar(1L, dto);

        verify(clienteMapper).atualizarEntity(eq(dto), eq(entidade));
        verify(clienteRepository).save(entidade);
    }

    @Test
    void deveRejeitarAtualizacaoQuandoClienteNaoExiste() {
        ClienteRequestDTO dto = clienteRequestDTO("111.444.777-35");
        given(clienteRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.atualizar(1L, dto))
                .isInstanceOf(ClienteNaoEncontradoException.class);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveRejeitarAtualizacaoQuandoCpfPertenceAOutroCliente() {
        ClienteRequestDTO dto = clienteRequestDTO("111.444.777-35");
        Cliente entidade = clienteEntity();
        given(clienteRepository.findById(1L)).willReturn(Optional.of(entidade));
        given(clienteRepository.existsByCpfAndIdNot("11144477735", 1L)).willReturn(true);

        assertThatThrownBy(() -> clienteService.atualizar(1L, dto)).isInstanceOf(CpfDuplicadoException.class);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveExcluirQuandoEncontrado() {
        Cliente entidade = clienteEntity();
        given(clienteRepository.findById(1L)).willReturn(Optional.of(entidade));

        clienteService.excluir(1L);

        verify(clienteRepository).delete(entidade);
    }

    @Test
    void deveRejeitarExclusaoQuandoNaoEncontrado() {
        given(clienteRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.excluir(1L)).isInstanceOf(ClienteNaoEncontradoException.class);
        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    private ClienteRequestDTO clienteRequestDTO(String cpf) {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNome("João da Silva");
        dto.setCpf(cpf);
        return dto;
    }

    private Cliente clienteEntity() {
        Endereco endereco = new Endereco("01310100", "Av. Paulista", "Bela Vista", "São Paulo", "SP", null);
        Cliente cliente = new Cliente("João da Silva", "11144477735", endereco);
        cliente.setId(1L);
        return cliente;
    }

    private ClienteResponseDTO clienteResponseDTO() {
        return new ClienteResponseDTO(1L, "João da Silva", "111.***.***-35", null, null, null);
    }
}
