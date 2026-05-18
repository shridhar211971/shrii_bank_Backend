package com.shrii.bank.transaction.services;

import com.shrii.bank.account.entity.Account;
import com.shrii.bank.account.repo.AccountRepo;
import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.auth_users.services.UserService;
import com.shrii.bank.enums.TransactionStatus;
import com.shrii.bank.enums.TransactionType;
import com.shrii.bank.exceptions.NotFoundException;
import com.shrii.bank.res.Response;
import com.shrii.bank.transaction.dtos.TransactionDTO;
import com.shrii.bank.transaction.dtos.TransactionRequest;
import com.shrii.bank.transaction.entity.Transaction;
import com.shrii.bank.transaction.repo.TransactionRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import com.shrii.bank.exceptions.BadRequestException;
import com.shrii.bank.notification.services.NotificationService;
import com.shrii.bank.notification.dtos.NotificationDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final AccountRepo accountRepo;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Response<?> createTransaction(TransactionRequest transactionRequest) {

        Transaction transaction = new Transaction();

        transaction.setTransactionType(
                transactionRequest.getTransactionType()
        );

        transaction.setAmount(
                transactionRequest.getAmount()
        );

        transaction.setDescription(
                transactionRequest.getDescription()
        );

        switch (transactionRequest.getTransactionType()) {

            case DEPOSIT ->
                    handleDeposit(transactionRequest, transaction);

            case WITHDRAWAL ->
                    handleWithdrawal(transactionRequest, transaction);

            case TRANSFER ->
                    handleTransfer(transactionRequest, transaction);

            default ->
                    throw new RuntimeException("Invalid transaction type");
        }

        transaction.setStatus(TransactionStatus.SUCCESS);

        Transaction savedTxn = transactionRepo.save(transaction);

        sendTransactionNotifications(savedTxn);

        return Response.builder()
                .statusCode(200)
                .message("Transaction successful")
                .build();
    }

    @Override
    public Response<List<TransactionDTO>> getTransactionsForMyAccount(
            String accountNumber,
            int page,
            int size
    ) {

        // currently logged in user
        User user = userService.getCurrentLoggedInUser();

        // find account
        Account account = accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new NotFoundException("Account not found")
                );

        // security check
        if (!account.getUser().getId().equals(user.getId())) {

        	throw new RuntimeException(
                    "Account does not belong to authenticated user"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("transactionDate").descending()
        );

        Page<Transaction> txns =
                transactionRepo.findByAccount_AccountNumber(
                        accountNumber,
                        pageable
                );

        List<TransactionDTO> transactionDTOs =
                txns.getContent()
                        .stream()
                        .map(transaction ->
                                modelMapper.map(
                                        transaction,
                                        TransactionDTO.class
                                )
                        )
                        .toList();

        return Response.<List<TransactionDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transactions retrieved successfully")
                .data(transactionDTOs)
                .meta(Map.of(
                        "currentPage", txns.getNumber(),
                        "totalItems", txns.getTotalElements(),
                        "totalPages", txns.getTotalPages(),
                        "pageSize", txns.getSize()
                ))
                .build();
    }

    // ==========================
    // DEPOSIT
    // ==========================

    private void handleDeposit(
            TransactionRequest request,
            Transaction transaction
    ) {

        Account account = accountRepo
                .findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() ->
                        new NotFoundException("Account not found"));

        account.setBalance(
                account.getBalance().add(request.getAmount())
        );

        transaction.setAccount(account);

        accountRepo.save(account);
    }

    // ==========================
    // WITHDRAWAL
    // ==========================

    private void handleWithdrawal(
            TransactionRequest request,
            Transaction transaction
    ) {

        Account account = accountRepo
                .findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() ->
                        new NotFoundException("Account not found"));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {

            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(
                account.getBalance().subtract(request.getAmount())
        );

        transaction.setAccount(account);

        accountRepo.save(account);
    }

    // ==========================
    // TRANSFER
    // ==========================

    private void handleTransfer(
            TransactionRequest request,
            Transaction transaction
    ) {

        Account sourceAccount = accountRepo
                .findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() ->
                        new NotFoundException("Source account not found"));

        Account destination = accountRepo
                .findByAccountNumber(
                        request.getDestinationAccountNumber()
                )
                .orElseThrow(() ->
                        new NotFoundException("Destination account not found"));

        if (sourceAccount.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new RuntimeException(
                    "Insufficient balance in source account"
            );
        }

        // deduct from source
        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(request.getAmount())
        );

        accountRepo.save(sourceAccount);

        // add to destination
        destination.setBalance(
                destination.getBalance()
                        .add(request.getAmount())
        );

        accountRepo.save(destination);

        transaction.setAccount(sourceAccount);

        transaction.setSourceAccount(
                sourceAccount.getAccountNumber()
        );

        transaction.setDestinationAccount(
                destination.getAccountNumber()
        );
    }

    // ==========================
    // SEND NOTIFICATION
    // ==========================

   private void sendTransactionNotifications(Transaction txn) {

    User user = txn.getAccount().getUser();

    String subject;
    String template;

    Map<String, Object> templateVariables =
            new HashMap<>();

    templateVariables.put(
            "name",
            user.getFirstName()
    );

    templateVariables.put(
            "amount",
            txn.getAmount()
    );

    templateVariables.put(
            "accountNumber",
            txn.getAccount().getAccountNumber()
    );

    templateVariables.put(
            "date",
            txn.getTransactionDate()
    );

    templateVariables.put(
            "balance",
            txn.getAccount().getBalance()
    );

    // ======================
    // DEPOSIT
    // ======================

    if (txn.getTransactionType() == TransactionType.DEPOSIT) {

        subject = "Credit Alert";
        template = "credit-alert";

        NotificationDTO notification =
                NotificationDTO.builder()
                        .recipient(user.getEmail())
                        .subject(subject)
                        .templateName(template)
                        .templateVariables(templateVariables)
                        .build();

        notificationService.sendEmail(notification, user);

    }

    // ======================
    // WITHDRAWAL
    // ======================

    else if (txn.getTransactionType()
            == TransactionType.WITHDRAWAL) {

        subject = "Debit Alert";
        template = "debit-alert";

        NotificationDTO notification =
                NotificationDTO.builder()
                        .recipient(user.getEmail())
                        .subject(subject)
                        .templateName(template)
                        .templateVariables(templateVariables)
                        .build();

        notificationService.sendEmail(notification, user);

    }

    // ======================
    // TRANSFER
    // ======================

    else if (txn.getTransactionType()
            == TransactionType.TRANSFER) {

        // sender email

        subject = "Transfer Alert";
        template = "debit-alert";

        NotificationDTO senderNotification =
                NotificationDTO.builder()
                        .recipient(user.getEmail())
                        .subject(subject)
                        .templateName(template)
                        .templateVariables(templateVariables)
                        .build();

        notificationService.sendEmail(
                senderNotification,
                user
        );

        // receiver email

        Account destination =
                accountRepo.findByAccountNumber(
                        txn.getDestinationAccount()
                ).orElseThrow(() ->
                        new NotFoundException(
                                "Destination account not found"
                        )
                );

        User receiver = destination.getUser();

        Map<String, Object> recvVars =
                new HashMap<>();

        recvVars.put(
                "name",
                receiver.getFirstName()
        );

        recvVars.put(
                "amount",
                txn.getAmount()
        );

        recvVars.put(
                "accountNumber",
                destination.getAccountNumber()
        );

        recvVars.put(
                "date",
                txn.getTransactionDate()
        );

        recvVars.put(
                "balance",
                destination.getBalance()
        );

        NotificationDTO receiverNotification =
                NotificationDTO.builder()
                        .recipient(receiver.getEmail())
                        .subject("Credit Alert")
                        .templateName("credit-alert")
                        .templateVariables(recvVars)
                        .build();

        notificationService.sendEmail(
                receiverNotification,
                receiver
        );
    }
}
}