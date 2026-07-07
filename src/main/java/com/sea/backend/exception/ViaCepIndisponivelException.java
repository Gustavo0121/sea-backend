package com.sea.backend.exception;

public class ViaCepIndisponivelException extends RuntimeException {

    public ViaCepIndisponivelException(Throwable cause) {
        super("Serviço de consulta de CEP indisponível no momento.", cause);
    }
}
