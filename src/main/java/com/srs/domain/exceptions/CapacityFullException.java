package com.srs.domain.exceptions;

public class CapacityFullException extends RuntimeException {

    public CapacityFullException(String message) {
        super(message);
    }
}
