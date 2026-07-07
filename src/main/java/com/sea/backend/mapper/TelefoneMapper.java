package com.sea.backend.mapper;

import com.sea.backend.dto.TelefoneRequestDTO;
import com.sea.backend.dto.TelefoneResponseDTO;
import com.sea.backend.entity.Telefone;
import com.sea.backend.utils.DigitExtractor;
import com.sea.backend.utils.MaskUtils;
import org.springframework.stereotype.Component;

@Component
public class TelefoneMapper {

    public Telefone toEntity(TelefoneRequestDTO dto) {
        return new Telefone(dto.getTipo(), DigitExtractor.onlyDigits(dto.getNumero()));
    }

    public TelefoneResponseDTO toResponseDTO(Telefone telefone) {
        return new TelefoneResponseDTO(
                telefone.getTipo(),
                MaskUtils.maskTelefone(telefone.getTipo(), telefone.getNumero())
        );
    }
}
