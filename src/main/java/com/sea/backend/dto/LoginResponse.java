package com.sea.backend.dto;

public class LoginResponse {

    private final String token;
    private final String tipo;
    private final long expiraEmSegundos;

    public LoginResponse(String token, String tipo, long expiraEmSegundos) {
        this.token = token;
        this.tipo = tipo;
        this.expiraEmSegundos = expiraEmSegundos;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public long getExpiraEmSegundos() {
        return expiraEmSegundos;
    }
}
