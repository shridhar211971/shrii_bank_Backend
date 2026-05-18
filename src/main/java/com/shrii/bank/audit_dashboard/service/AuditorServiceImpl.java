package com.shrii.bank.audit_dashboard.service;

import com.shrii.bank.account.dtos.AccountDTO;
import com.shrii.bank.account.entity.Account;
import com.shrii.bank.account.repo.AccountRepo;
import com.shrii.bank.auth_users.dtos.UserDTO;
import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.auth_users.repo.UserRepo;
import com.shrii.bank.transaction.dtos.TransactionDTO;
import com.shrii.bank.transaction.entity.Transaction;
import com.shrii.bank.transaction.repo.TransactionRepo;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditorServiceImpl implements AuditorService {

    private final UserRepo userRepo;
    private final AccountRepo accountRepo;
    private final TransactionRepo transactionRepo;
    private final ModelMapper modelMapper;

    @Override
    public Map<String, Long> getSystemTotals() {

        long totalUsers = userRepo.count();

        long totalAccounts = accountRepo.count();

        long totalTransactions = transactionRepo.count();

        return Map.of(
                "totalUsers", totalUsers,
                "totalAccounts", totalAccounts,
                "totalTransactions", totalTransactions
        );
    }

    @Override
    public Optional<UserDTO> findUserByEmail(String email) {

        return userRepo.findByEmail(email)
                .map(user ->
                        modelMapper.map(
                                user,
                                UserDTO.class
                        )
                );
    }

    @Override
    public Optional<AccountDTO> findAccountDetailsByAccountNumber(
            String accountNumber
    ) {

        return accountRepo.findByAccountNumber(accountNumber)
                .map(account ->
                        modelMapper.map(
                                account,
                                AccountDTO.class
                        )
                );
    }

    @Override
    public List<TransactionDTO> findTransactionsByAccountNumber(
            String accountNumber
    ) {

        return transactionRepo
                .findByAccount_AccountNumber(accountNumber)
                .stream()
                .map(transaction ->
                        modelMapper.map(
                                transaction,
                                TransactionDTO.class
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TransactionDTO> findTransactionById(
            Long transactionId
    ) {

        return transactionRepo.findById(transactionId)
                .map(transaction ->
                        modelMapper.map(
                                transaction,
                                TransactionDTO.class
                        )
                );
    }
}