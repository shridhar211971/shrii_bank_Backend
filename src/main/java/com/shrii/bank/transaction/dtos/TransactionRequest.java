package com.shrii.bank.transaction.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shrii.bank.enums.TransactionType;

import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest {

    private TransactionType transactionType;

    private BigDecimal amount;

    private String accountNumber;

    private String description;

    // receiving account number for transfer
    private String destinationAccountNumber;

}