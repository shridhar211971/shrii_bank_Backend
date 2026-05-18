package com.shrii.bank.account.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.shrii.bank.auth_users.dtos.UserDTO;
import com.shrii.bank.enums.AccountStatus;
import com.shrii.bank.enums.AccountType;
import com.shrii.bank.enums.Currency;
import com.shrii.bank.transaction.dtos.TransactionDTO;
import com.shrii.bank.transaction.entity.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {

    private Long id;

    private String accountNumber;

    private BigDecimal balance;

    private AccountType accountType;

    private UserDTO user;

    private Currency currency;

    private AccountStatus status;

    private List<Transaction> transactions;

    private LocalDateTime closedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}