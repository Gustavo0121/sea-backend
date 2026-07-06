package com.sea.backend.dto;

import com.sea.backend.entity.TipoTelefone;
import com.sea.backend.validation.TelefoneValido;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@TelefoneValido
public class TelefoneRequestDTO {

    @NotNull(message = "Tipo de telefone é obrigatório.")
    private TipoTelefone tipo;

    @NotBlank(message = "Número de telefone é obrigatório.")
    private String numero;

    public TelefoneRequestDTO() {
    }

    public TipoTelefone getTipo() {
        return tipo;
    }

    public void setTipo(TipoTelefone tipo) {
        this.tipo = tipo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
