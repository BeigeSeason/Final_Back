package com.springboot.final_back.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class BanUserException extends ResponseStatusException {
    public BanUserException(HttpStatus status, String reason) {
        super(status, reason);
    }
}