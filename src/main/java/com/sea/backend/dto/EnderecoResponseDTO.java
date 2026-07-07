package com.sea.backend.dto;

public class EnderecoResponseDTO {

    private final String cep;
    private final String logradouro;
    private final String bairro;
    private final String cidade;
    private final String uf;
    private final String complemento;

    public EnderecoResponseDTO(String cep, String logradouro, String bairro, String cidade, String uf, String complemento) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.complemento = complemento;
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public String getComplemento() {
        return complemento;
    }
}
