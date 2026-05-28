package com.shrii.bank.account.controller;

import com.shrii.bank.account.services.AccountService;
import com.shrii.bank.res.Response;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
//@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // GET MY ACCOUNTS
    @GetMapping("/me")
    public ResponseEntity<Response<?>> getMyAccounts() {

        return ResponseEntity.ok(
                accountService.getMyAccounts()
        );
    }

    // CLOSE ACCOUNT
    @DeleteMapping("/close/{accountNumber}")
    public ResponseEntity<Response<?>> closeAccount(
            @PathVariable String accountNumber
    ) {

        return ResponseEntity.ok(
                accountService.closeAccount(accountNumber)
        );
    }
}