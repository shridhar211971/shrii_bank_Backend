package com.shrii.bank.transaction.services;

import com.shrii.bank.res.Response;
import com.shrii.bank.transaction.dtos.TransactionDTO;
import com.shrii.bank.transaction.dtos.TransactionRequest;

import java.util.List;

public interface TransactionService {

    Response<?> createTransaction(TransactionRequest request);

    Response<List<TransactionDTO>> getTransactionsForMyAccount(
            String accountNumber,
            int page,
            int size
    );
}