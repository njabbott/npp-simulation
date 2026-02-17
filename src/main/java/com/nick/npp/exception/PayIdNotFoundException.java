package com.nick.npp.exception;

public class PayIdNotFoundException extends RuntimeException {
    public PayIdNotFoundException(String message) {
        super(message);
    }
}
