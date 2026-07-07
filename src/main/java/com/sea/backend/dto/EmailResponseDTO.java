package com.sea.backend.dto;

public class EmailResponseDTO {

    private final String endereco;

    public EmailResponseDTO(String endereco) {
        this.endereco = endereco;
    }

    public String getEndereco() {
        return endereco;
    }
}
