package com.shrii.bank.auth_users.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {

    // used for forgot password request
    private String email;

    // used for setting new password
    private String code;

    private String newPassword;

}