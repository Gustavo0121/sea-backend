package com.sea.backend.mapper;

import com.sea.backend.dto.EnderecoRequestDTO;
import com.sea.backend.dto.EnderecoResponseDTO;
import com.sea.backend.entity.Endereco;
import com.sea.backend.utils.DigitExtractor;
import com.sea.backend.utils.MaskUtils;
import com.sea.backend.utils.TextSanitizer;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {

    public Endereco toEntity(EnderecoRequestDTO dto) {
        return new Endereco(
                DigitExtractor.onlyDigits(dto.getCep()),
                TextSanitizer.sanitize(dto.getLogradouro()),
                TextSanitizer.sanitize(dto.getBairro()),
                TextSanitizer.sanitize(dto.getCidade()),
                dto.getUf().toUpperCase(),
                TextSanitizer.sanitize(dto.getComplemento())
        );
    }

    public void atualizarEntity(EnderecoRequestDTO dto, Endereco endereco) {
        endereco.setCep(DigitExtractor.onlyDigits(dto.getCep()));
        endereco.setLogradouro(TextSanitizer.sanitize(dto.getLogradouro()));
        endereco.setBairro(TextSanitizer.sanitize(dto.getBairro()));
        endereco.setCidade(TextSanitizer.sanitize(dto.getCidade()));
        endereco.setUf(dto.getUf().toUpperCase());
        endereco.setComplemento(TextSanitizer.sanitize(dto.getComplemento()));
    }

    public EnderecoResponseDTO toResponseDTO(Endereco endereco) {
        return new EnderecoResponseDTO(
                MaskUtils.maskCep(endereco.getCep()),
                endereco.getLogradouro(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf(),
                endereco.getComplemento()
        );
    }
}
