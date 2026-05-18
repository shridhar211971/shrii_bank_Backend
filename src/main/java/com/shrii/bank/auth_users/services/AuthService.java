package com.shrii.bank.auth_users.services;

import com.shrii.bank.auth_users.dtos.LoginRequest;
import com.shrii.bank.auth_users.dtos.LoginResponse;
import com.shrii.bank.auth_users.dtos.RegistrationRequest;
import com.shrii.bank.auth_users.dtos.ResetPasswordRequest;
import com.shrii.bank.res.Response;

public interface AuthService {

    Response<String> register(
            RegistrationRequest request
    );

    Response<LoginResponse> login(
            LoginRequest loginRequest
    );

    Response<?> forgotPassword(
            String email
    );

    Response<?> updatePasswordViaResetCode(
            ResetPasswordRequest resetPasswordRequest
    );
}