package com.shrii.bank.audit_dashboard.service;

import com.shrii.bank.account.dtos.AccountDTO;
import com.shrii.bank.auth_users.dtos.UserDTO;
import com.shrii.bank.transaction.dtos.TransactionDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AuditorService {

    Map<String, Long> getSystemTotals();

    Optional<UserDTO> findUserByEmail(String email);

    Optional<AccountDTO> findAccountDetailsByAccountNumber(
            String accountNumber
    );

    List<TransactionDTO> findTransactionsByAccountNumber(
            String accountNumber
    );

    Optional<TransactionDTO> findTransactionById(
            Long transactionId
    );
}