package com.sea.backend.dto;

import com.sea.backend.entity.TipoTelefone;

public class TelefoneResponseDTO {

    private final TipoTelefone tipo;
    private final String numero;

    public TelefoneResponseDTO(TipoTelefone tipo, String numero) {
        this.tipo = tipo;
        this.numero = numero;
    }

    public TipoTelefone getTipo() {
        return tipo;
    }

    public String getNumero() {
        return numero;
    }
}
