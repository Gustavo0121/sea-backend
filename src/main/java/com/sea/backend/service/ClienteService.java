package com.sea.backend.service;

import com.sea.backend.dto.ClienteRequestDTO;
import com.sea.backend.dto.ClienteResponseDTO;
import com.sea.backend.entity.Cliente;
import com.sea.backend.exception.ClienteNaoEncontradoException;
import com.sea.backend.exception.CpfDuplicadoException;
import com.sea.backend.mapper.ClienteMapper;
import com.sea.backend.repository.ClienteRepository;
import com.sea.backend.utils.DigitExtractor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    public ClienteResponseDTO cadastrar(ClienteRequestDTO dto) {
        String cpf = DigitExtractor.onlyDigits(dto.getCpf());
        if (clienteRepository.existsByCpf(cpf)) {
            throw new CpfDuplicadoException();
        }
        Cliente cliente = clienteMapper.toEntity(dto);
        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteMapper.toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(String nome, String cpf, Pageable pageable) {
        String nomeFiltro = nome == null ? "" : nome;
        String cpfFiltro = DigitExtractor.onlyDigits(cpf);
        return clienteRepository
                .findByNomeContainingIgnoreCaseAndCpfContaining(nomeFiltro, cpfFiltro, pageable)
                .map(clienteMapper::toResponseDTO);
    }

    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = buscarEntidadePorId(id);
        String cpf = DigitExtractor.onlyDigits(dto.getCpf());
        if (clienteRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new CpfDuplicadoException();
        }
        clienteMapper.atualizarEntity(dto, cliente);
        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    public void excluir(Long id) {
        clienteRepository.delete(buscarEntidadePorId(id));
    }

    private Cliente buscarEntidadePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }
}
