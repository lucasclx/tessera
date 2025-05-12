package com.backend.tessera.versao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VersaoNotFoundException extends RuntimeException {
    public VersaoNotFoundException(String message) {
        super(message);
    }
}