package com.wsd.ska.exceptions;

import org.springframework.security.core.AuthenticationException;

public class UserIsInactiveException extends AuthenticationException {

    public UserIsInactiveException(String msg) {
        super(msg);
    }
}
