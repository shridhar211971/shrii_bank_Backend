package com.shrii.bank.account.services;

import com.shrii.bank.account.dtos.AccountDTO;
import com.shrii.bank.account.entity.Account;
import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.enums.AccountType;
import com.shrii.bank.res.Response;

import java.util.List;

public interface AccountService {

    Account createAccount(AccountType accountType, User user);

    Response<List<AccountDTO>> getMyAccounts();

    Response<?> closeAccount(String accountNumber);
}