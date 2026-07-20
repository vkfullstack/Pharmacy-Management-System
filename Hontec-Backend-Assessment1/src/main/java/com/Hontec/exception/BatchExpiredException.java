package com.Hontec.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BatchExpiredException extends RuntimeException {
    public BatchExpiredException(String message) {
        super(message);
    }
}
