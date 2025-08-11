package com.ecommerce.userservice.exception;

import org.aspectj.bridge.IMessage;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
