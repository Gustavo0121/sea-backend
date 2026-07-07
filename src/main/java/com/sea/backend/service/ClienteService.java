package com.sea.backend.service;

import com.sea.backend.dto.ClienteRequestDTO;
import com.sea.backend.dto.ClienteResponseDTO;
import com.sea.backend.entity.Cliente;
import com.sea.backend.exception.ClienteNaoEncontradoException;
import com.sea.backend.exception.CpfDuplicadoException;
import com.sea.backend.mapper.ClienteMapper;
import com.sea.backend.repository.ClienteRepository;
import com.sea.backend.utils.DigitExtractor;
import com.sea.backend.utils.MaskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Audit logs here only ever carry the cliente id and the masked CPF (MaskUtils.maskCpf) —
 * the raw CPF digits are never passed to the logger.
 */
@Service
@Transactional
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

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
        Cliente salvo = clienteRepository.save(cliente);
        log.info("Cliente cadastrado: id={}, cpf={}.", salvo.getId(), MaskUtils.maskCpf(cpf));
        return clienteMapper.toResponseDTO(salvo);
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
        Cliente salvo = clienteRepository.save(cliente);
        log.info("Cliente atualizado: id={}, cpf={}.", salvo.getId(), MaskUtils.maskCpf(cpf));
        return clienteMapper.toResponseDTO(salvo);
    }

    public void excluir(Long id) {
        Cliente cliente = buscarEntidadePorId(id);
        clienteRepository.delete(cliente);
        log.info("Cliente excluído: id={}.", id);
    }

    private Cliente buscarEntidadePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }
}
