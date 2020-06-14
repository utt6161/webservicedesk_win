package com.wsd.ska.exceptions;

import org.springframework.security.core.AuthenticationException;

public class UserIsNotValidException extends AuthenticationException {

    public UserIsNotValidException(String msg) {
        super(msg);
    }
}