package com.sea.backend.dto;

import com.sea.backend.validation.CpfValido;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

public class ClienteRequestDTO {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    @Pattern(regexp = "^[\\p{L}0-9 ]+$", message = "Nome deve conter apenas letras, números e espaços.")
    private String nome;

    @NotBlank(message = "CPF é obrigatório.")
    @CpfValido
    private String cpf;

    @NotNull(message = "Endereço é obrigatório.")
    @Valid
    private EnderecoRequestDTO endereco;

    @NotEmpty(message = "Pelo menos um telefone é obrigatório.")
    @Valid
    private List<TelefoneRequestDTO> telefones;

    @NotEmpty(message = "Pelo menos um email é obrigatório.")
    @Valid
    private List<EmailRequestDTO> emails;

    public ClienteRequestDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public EnderecoRequestDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoRequestDTO endereco) {
        this.endereco = endereco;
    }

    public List<TelefoneRequestDTO> getTelefones() {
        return telefones;
    }

    public void setTelefones(List<TelefoneRequestDTO> telefones) {
        this.telefones = telefones;
    }

    public List<EmailRequestDTO> getEmails() {
        return emails;
    }

    public void setEmails(List<EmailRequestDTO> emails) {
        this.emails = emails;
    }
}
