package com.springboot.final_back.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NotMemberException extends ResponseStatusException {
    public NotMemberException(HttpStatus status, String reason) {
        super(status, reason);
    }

}
