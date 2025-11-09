package com.filadelfia.store.filadelfiastore.exception.custom;

public class NoHandlerFoundException extends RuntimeException {
    public NoHandlerFoundException(String message) {
        super(message);
    }
}