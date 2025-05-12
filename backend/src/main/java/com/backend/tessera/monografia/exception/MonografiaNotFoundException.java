package com.backend.tessera.monografia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MonografiaNotFoundException extends RuntimeException {
    public MonografiaNotFoundException(String message) {
        super(message);
    }
}