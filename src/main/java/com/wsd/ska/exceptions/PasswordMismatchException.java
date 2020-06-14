package com.wsd.ska.exceptions;

import org.springframework.security.core.AuthenticationException;

public class PasswordMismatchException extends AuthenticationException {
    public PasswordMismatchException(String explanation) {
        super(explanation);
    }
}
