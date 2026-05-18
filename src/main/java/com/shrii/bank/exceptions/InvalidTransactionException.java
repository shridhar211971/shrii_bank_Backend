package com.shrii.bank.exceptions;

public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String error) {
        super(error);
    }

}