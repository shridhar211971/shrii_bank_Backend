package com.shrii.bank.account.services;


import com.shrii.bank.account.dtos.AccountDTO;
import com.shrii.bank.account.entity.Account;
import com.shrii.bank.account.repo.AccountRepo;
import com.shrii.bank.account.services.AccountService;
import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.auth_users.services.UserService;
import com.shrii.bank.enums.AccountStatus;
import com.shrii.bank.enums.AccountType;
import com.shrii.bank.enums.Currency;
import com.shrii.bank.exceptions.BadRequestException;
import com.shrii.bank.exceptions.NotFoundException;
import com.shrii.bank.res.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepo accountRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

    private final Random random = new Random();

    // CREATE ACCOUNT
    @Override
    public Account createAccount(AccountType accountType, User user) {
    	log.info("Inside createAccount()");

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .currency(Currency.INR)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        return accountRepo.save(account);
    }

    // GET MY ACCOUNTS
    @Override
    public Response<List<AccountDTO>> getMyAccounts() {

        User user = userService.getCurrentLoggedInUser();

        List<AccountDTO> accounts = accountRepo
                .findByUserId(user.getId())
                .stream()
                .map(account ->
                        modelMapper.map(account, AccountDTO.class)
                )
                .toList();

        return Response.<List<AccountDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User accounts fetched successfully")
                .data(accounts)
                .build();
    }

    // CLOSE ACCOUNT
    @Override
    public Response<?> closeAccount(String accountNumber) {

        User user = userService.getCurrentLoggedInUser();

        Account account = accountRepo
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new NotFoundException("Account Not Found")
                );

        if (!user.getAccounts().contains(account)) {
            throw new NotFoundException(
                    "Account doesn't belong to you"
            );
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {

            throw new BadRequestException(
                    "Account balance must be zero before closing"
            );
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());

        accountRepo.save(account);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account closed successfully")
                .build();
    }

    // GENERATE ACCOUNT NUMBER
    private String generateAccountNumber() {
    	

        String accountNumber;

        do {

            // random 8 digit number
            accountNumber =
                    "66" + (random.nextInt(90000000) + 10000000);

        } while (
                accountRepo
                        .findByAccountNumber(accountNumber)
                        .isPresent()
        );
        log.info("Inside Number generated{}, accountRepo");
        return accountNumber;
    }
}