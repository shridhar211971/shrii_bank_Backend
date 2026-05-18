package com.shrii.bank.transaction.controller;

import com.shrii.bank.res.Response;
import com.shrii.bank.transaction.dtos.TransactionRequest;
import com.shrii.bank.transaction.services.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Response<?>> createTransaction(
            @RequestBody @Valid TransactionRequest request
    ) {

        return ResponseEntity.ok(
                transactionService.createTransaction(request)
        );
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Response<?>> getTransactionsForMyAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {

        return ResponseEntity.ok(
                transactionService.getTransactionsForMyAccount(
                        accountNumber,
                        page,
                        size
                )
        );
    }
}