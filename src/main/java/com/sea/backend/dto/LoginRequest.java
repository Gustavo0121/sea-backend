package com.sea.backend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "Login é obrigatório.")
    @Size(max = 50, message = "Login deve ter no máximo 50 caracteres.")
    private String login;

    @NotBlank(message = "Senha é obrigatória.")
    @Size(max = 100, message = "Senha deve ter no máximo 100 caracteres.")
    private String senha;

    public LoginRequest() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
