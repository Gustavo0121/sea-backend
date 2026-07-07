package com.sea.backend.mapper;

import com.sea.backend.dto.EmailRequestDTO;
import com.sea.backend.dto.EmailResponseDTO;
import com.sea.backend.entity.Email;
import org.springframework.stereotype.Component;

@Component
public class EmailMapper {

    public Email toEntity(EmailRequestDTO dto) {
        return new Email(dto.getEndereco().trim());
    }

    public EmailResponseDTO toResponseDTO(Email email) {
        return new EmailResponseDTO(email.getEndereco());
    }
}
