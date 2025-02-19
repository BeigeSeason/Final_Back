package com.springboot.final_back.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BanUserException.class)
    public ResponseEntity<String> handleBanUserException(BanUserException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getReason());
    }

    @ExceptionHandler(NotMemberException.class)
    public ResponseEntity<String> handleNotMemberException(NotMemberException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getReason());
    }

    @ExceptionHandler(SignOutException.class)
    public ResponseEntity<String> handleSignOutException(SignOutException e) {
        return ResponseEntity.status(HttpStatus.GONE).body(e.getReason());
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<String> handlePasswordNotMatchException(PasswordNotMatchException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getReason());
    }
}
