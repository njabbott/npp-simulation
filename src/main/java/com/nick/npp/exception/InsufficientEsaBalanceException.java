package com.nick.npp.exception;

public class InsufficientEsaBalanceException extends RuntimeException {
    public InsufficientEsaBalanceException(String message) {
        super(message);
    }
}
