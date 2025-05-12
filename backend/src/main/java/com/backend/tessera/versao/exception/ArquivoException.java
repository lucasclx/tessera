package com.backend.tessera.versao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Ou um status mais apropriado
public class ArquivoException extends RuntimeException {
    public ArquivoException(String message) {
        super(message);
    }

    public ArquivoException(String message, Throwable cause) {
        super(message, cause);
    }
}