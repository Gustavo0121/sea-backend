package com.sea.backend.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class EmailRequestDTO {

    @NotBlank(message = "Email é obrigatório.")
    @Email(message = "Email inválido.")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres.")
    private String endereco;

    public EmailRequestDTO() {
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}
