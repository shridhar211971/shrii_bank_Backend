package com.shrii.bank.exceptions;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String error) {
        super(error);
    }

}