package com.shrii.bank.auth_users.services.impl;

import com.shrii.bank.account.entity.Account;
import com.shrii.bank.account.services.AccountService;
import com.shrii.bank.auth_users.dtos.LoginRequest;
import com.shrii.bank.auth_users.dtos.LoginResponse;
import com.shrii.bank.auth_users.dtos.RegistrationRequest;
import com.shrii.bank.auth_users.dtos.ResetPasswordRequest;
import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.auth_users.repo.UserRepo;
import com.shrii.bank.auth_users.services.AuthService;
import com.shrii.bank.enums.AccountType;
import com.shrii.bank.enums.Currency;
import com.shrii.bank.exceptions.BadRequestException;
import com.shrii.bank.exceptions.NotFoundException;
import com.shrii.bank.notification.dtos.NotificationDTO;
import com.shrii.bank.notification.repo.NotificationRepo;
import com.shrii.bank.notification.services.NotificationService;
import com.shrii.bank.res.Response;
import com.shrii.bank.role.entity.Role;
import com.shrii.bank.role.repo.RoleRepo;
import com.shrii.bank.security.TokenService;
import com.shrii.bank.auth_users.entity.PasswordResetCode;
import com.shrii.bank.auth_users.repo.PasswordResetCodeRepo;
import com.shrii.bank.auth_users.services.CodeGenerator;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AccountService accountService;
    private final TokenService tokenService;
    private final CodeGenerator codeGenerator;
    private final PasswordResetCodeRepo passwordResetCodeRepo;

    @Value("${password.reset.link}")
    private String resetLink;

    @Override
    public Response<String> register(
            RegistrationRequest request
    ) {

        List<Role> roles;

        // DEFAULT ROLE
        if (request.getRoles() == null || request.getRoles().isEmpty()) {

            Role defaultRole = roleRepo.findByName("CUSTOMER")
                    .orElseThrow(() ->
                            new NotFoundException("CUSTOMER ROLE NOT FOUND"));

            roles = Collections.singletonList(defaultRole);

        } else {

            roles = request.getRoles()
                    .stream()
                    .map(roleName ->
                            roleRepo.findByName(roleName)
                                    .orElseThrow(() ->
                                            new NotFoundException(
                                                    "ROLE NOT FOUND : " + roleName
                                            )))
                    .toList();
        }

        // CHECK EMAIL
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {

            throw new BadRequestException(
                    "Email already exists"
            );
        }

        // SAVE USER
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .active(true)
                .build();

        User savedUser = userRepo.save(user);

        // CREATE ACCOUNT
        Account savedAccount =
                accountService.createAccount(
                        AccountType.SAVINGS,
                        savedUser
                );

        // WELCOME EMAIL
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", savedUser.getFirstName());

        NotificationDTO welcomeMail =
                NotificationDTO.builder()
                        .recipient(savedUser.getEmail())
                        .subject("Welcome to Shrii Bank 🎉")
                        .templateName("welcome-email")
                        .templateVariables(vars)
                        .build();

        notificationService.sendEmail(
                welcomeMail,
                savedUser
        );

        // ACCOUNT DETAILS EMAIL
        Map<String, Object> accountVars = new HashMap<>();

        accountVars.put("name",
                savedUser.getFirstName());

        accountVars.put("accountNumber",
                savedAccount.getAccountNumber());

        accountVars.put("accountType",
                AccountType.SAVINGS.name());

        accountVars.put("currency",
                Currency.USD.name());

        NotificationDTO accountCreatedMail =
                NotificationDTO.builder()
                        .recipient(savedUser.getEmail())
                        .subject("Your Bank Account Has Been Created ✅")
                        .templateName("account-created")
                        .templateVariables(accountVars)
                        .build();

        notificationService.sendEmail(
                accountCreatedMail,
                savedUser
        );

        return Response.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User registered successfully")
                .data(
                        "Account Number : "
                                + savedAccount.getAccountNumber()
                )
                .build();
    }

    @Override
    public Response<LoginResponse> login(
            LoginRequest loginRequest
    ) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Email Not Found"
                        ));

        if (!passwordEncoder.matches(
                password,
                user.getPassword()
        )) {

            throw new BadRequestException(
                    "Password doesn't match"
            );
        }

        String token =
                tokenService.generateToken(
                        user.getEmail()
                );

        LoginResponse loginResponse =
                LoginResponse.builder()
                        .roles(
                                user.getRoles()
                                        .stream()
                                        .map(Role::getName)
                                        .toList()
                        )
                        .token(token)
                        .build();

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login Successful")
                .data(loginResponse)
                .build();
    }

    @Override
    @Transactional
    public Response<?> forgotPassword(
            String email
    ) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User Not Found"
                        ));

        passwordResetCodeRepo.deleteByUserId(
                user.getId()
        );

        String code =
                codeGenerator.generateUniqueCode();

        PasswordResetCode resetCode =
                PasswordResetCode.builder()
                        .user(user)
                        .code(code)
                        .expiryDate(calculateExpiryDate())
                        .used(false)
                        .build();

        passwordResetCodeRepo.save(resetCode);

        // SEND EMAIL

        Map<String, Object> templateVariables =
                new HashMap<>();

        templateVariables.put(
                "name",
                user.getFirstName()
        );

        templateVariables.put(
                "resetLink",
                resetLink + code
        );

        NotificationDTO notificationDTO =
                NotificationDTO.builder()
                        .recipient(user.getEmail())
                        .subject("Password Reset Code")
                        .templateName("password-reset")
                        .templateVariables(templateVariables)
                        .build();

        notificationService.sendEmail(
                notificationDTO,
                user
        );

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message(
                        "Password reset code sent to your email"
                )
                .build();
    }

    @Override
    @Transactional
    public Response<?> updatePasswordViaResetCode(
            ResetPasswordRequest resetPasswordRequest
    ) {

        String code =
                resetPasswordRequest.getCode();

        String newPassword =
                resetPasswordRequest.getNewPassword();

        // FIND RESET CODE

        PasswordResetCode resetCode =
                passwordResetCodeRepo.findByCode(code)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid reset code"
                                ));

        // CHECK EXPIRATION

        if (resetCode.getExpiryDate().isBefore(
                LocalDateTime.now()
        )) {

            passwordResetCodeRepo.delete(resetCode);

            throw new BadRequestException(
                    "Reset code has expired"
            );
        }

        // UPDATE PASSWORD

        User user = resetCode.getUser();

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepo.save(user);

        // DELETE USED CODE

        passwordResetCodeRepo.delete(resetCode);

        // SEND CONFIRMATION EMAIL

        Map<String, Object> templateVariables =
                new HashMap<>();

        templateVariables.put(
                "name",
                user.getFirstName()
        );

        NotificationDTO confirmationEmail =
                NotificationDTO.builder()
                        .recipient(user.getEmail())
                        .subject("Password Updated Successfully")
                        .templateName(
                                "password-update-confirmation"
                        )
                        .templateVariables(templateVariables)
                        .build();

        notificationService.sendEmail(
                confirmationEmail,
                user
        );

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message(
                        "Password updated successfully"
                )
                .build();
    }
    
    private LocalDateTime calculateExpiryDate(){

        return LocalDateTime.now().plusHours(5);
    }
}