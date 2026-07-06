package com.sea.backend.mapper;

import com.sea.backend.dto.ClienteRequestDTO;
import com.sea.backend.dto.ClienteResponseDTO;
import com.sea.backend.entity.Cliente;
import com.sea.backend.entity.Email;
import com.sea.backend.entity.Telefone;
import com.sea.backend.utils.DigitExtractor;
import com.sea.backend.utils.MaskUtils;
import com.sea.backend.utils.TextSanitizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClienteMapper {

    private final EnderecoMapper enderecoMapper;
    private final TelefoneMapper telefoneMapper;
    private final EmailMapper emailMapper;

    public ClienteMapper(EnderecoMapper enderecoMapper, TelefoneMapper telefoneMapper, EmailMapper emailMapper) {
        this.enderecoMapper = enderecoMapper;
        this.telefoneMapper = telefoneMapper;
        this.emailMapper = emailMapper;
    }

    public Cliente toEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente(
                TextSanitizer.sanitize(dto.getNome()),
                DigitExtractor.onlyDigits(dto.getCpf()),
                enderecoMapper.toEntity(dto.getEndereco())
        );
        for (Telefone telefone : toTelefones(dto)) {
            cliente.addTelefone(telefone);
        }
        for (Email email : toEmails(dto)) {
            cliente.addEmail(email);
        }
        return cliente;
    }

    public ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                MaskUtils.maskCpf(cliente.getCpf()),
                enderecoMapper.toResponseDTO(cliente.getEndereco()),
                cliente.getTelefones().stream().map(telefoneMapper::toResponseDTO).collect(Collectors.toList()),
                cliente.getEmails().stream().map(emailMapper::toResponseDTO).collect(Collectors.toList())
        );
    }

    private List<Telefone> toTelefones(ClienteRequestDTO dto) {
        return dto.getTelefones().stream().map(telefoneMapper::toEntity).collect(Collectors.toList());
    }

    private List<Email> toEmails(ClienteRequestDTO dto) {
        return dto.getEmails().stream().map(emailMapper::toEntity).collect(Collectors.toList());
    }
}
