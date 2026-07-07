package com.sea.backend.mapper;

import com.sea.backend.dto.EmailRequestDTO;
import com.sea.backend.dto.EmailResponseDTO;
import com.sea.backend.entity.Email;
import com.sea.backend.utils.TextSanitizer;
import org.springframework.stereotype.Component;

@Component
public class EmailMapper {

    public Email toEntity(EmailRequestDTO dto) {
        return new Email(TextSanitizer.sanitize(dto.getEndereco()));
    }

    public EmailResponseDTO toResponseDTO(Email email) {
        return new EmailResponseDTO(email.getEndereco());
    }
}
