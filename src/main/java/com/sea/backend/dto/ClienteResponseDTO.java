package com.sea.backend.dto;

import java.util.List;

public class ClienteResponseDTO {

    private final Long id;
    private final String nome;
    private final String cpf;
    private final EnderecoResponseDTO endereco;
    private final List<TelefoneResponseDTO> telefones;
    private final List<EmailResponseDTO> emails;

    public ClienteResponseDTO(Long id, String nome, String cpf, EnderecoResponseDTO endereco,
                               List<TelefoneResponseDTO> telefones, List<EmailResponseDTO> emails) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.endereco = endereco;
        this.telefones = telefones;
        this.emails = emails;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public EnderecoResponseDTO getEndereco() {
        return endereco;
    }

    public List<TelefoneResponseDTO> getTelefones() {
        return telefones;
    }

    public List<EmailResponseDTO> getEmails() {
        return emails;
    }
}
