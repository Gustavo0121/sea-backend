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
