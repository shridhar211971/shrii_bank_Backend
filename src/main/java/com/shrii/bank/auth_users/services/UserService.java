package com.shrii.bank.auth_users.services;

import com.shrii.bank.auth_users.dtos.UpdatePasswordRequest;
import com.shrii.bank.auth_users.dtos.UserDTO;
import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.res.Response;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    User getCurrentLoggedInUser();

    Response<UserDTO> getMyProfile();

    Response<Page<UserDTO>> getAllUsers(
            int page,
            int size
    );

    Response<?> updatePassword(
            UpdatePasswordRequest updatePasswordRequest
    );

    Response<?> uploadProfilePicture(
            MultipartFile file
    );

    // NEW S3 METHOD
    Response<?> uploadProfilePictureToS3(
            MultipartFile file
    );
}