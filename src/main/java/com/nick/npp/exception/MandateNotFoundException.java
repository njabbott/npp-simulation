package com.nick.npp.exception;

public class MandateNotFoundException extends RuntimeException {
    public MandateNotFoundException(String message) {
        super(message);
    }
}
