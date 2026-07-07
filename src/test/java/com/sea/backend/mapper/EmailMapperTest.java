package com.sea.backend.mapper;

import com.sea.backend.dto.EmailRequestDTO;
import com.sea.backend.entity.Email;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailMapperTest {

    private final EmailMapper emailMapper = new EmailMapper();

    @Test
    void toEntityDeveRemoverCaracteresPerigososEEspacosDuplicados() {
        EmailRequestDTO dto = new EmailRequestDTO();
        dto.setEndereco("  joao<script>@example.com  ");

        Email email = emailMapper.toEntity(dto);

        assertThat(email.getEndereco()).isEqualTo("joaoscript@example.com");
    }
}
