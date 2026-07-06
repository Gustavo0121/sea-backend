package com.sea.backend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class EnderecoRequestDTO {

    @NotBlank(message = "CEP é obrigatório.")
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP deve estar no formato 00000-000 ou 00000000.")
    private String cep;

    @NotBlank(message = "Logradouro é obrigatório.")
    @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres.")
    private String logradouro;

    @NotBlank(message = "Bairro é obrigatório.")
    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres.")
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória.")
    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres.")
    private String cidade;

    @NotBlank(message = "UF é obrigatória.")
    @Pattern(regexp = "^(?i)[A-Z]{2}$", message = "UF deve conter exatamente 2 letras.")
    private String uf;

    @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres.")
    private String complemento;

    public EnderecoRequestDTO() {
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
}
