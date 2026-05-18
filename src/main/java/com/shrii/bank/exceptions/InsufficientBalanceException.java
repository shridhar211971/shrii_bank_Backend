package com.shrii.bank.exceptions;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String error) {
        super(error);
    }

}