package com.shrii.bank.auth_users.services.impl;

import com.shrii.bank.auth_users.dtos.UpdatePasswordRequest;
import com.shrii.bank.auth_users.dtos.UserDTO;
import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.auth_users.repo.UserRepo;
import com.shrii.bank.auth_users.services.UserService;
import com.shrii.bank.exceptions.BadRequestException;
import com.shrii.bank.exceptions.NotFoundException;
import com.shrii.bank.notification.services.NotificationService;
import com.shrii.bank.res.Response;
import com.shrii.bank.notification.dtos.NotificationDTO;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    private final NotificationService notificationService;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;
    
    private final String uploadDir = "uploads/profile-pictures/";

    @Override
    public User getCurrentLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {

            throw new NotFoundException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found"
                        ));
    }

    @Override
    public Response<UserDTO> getMyProfile() {

        User user = getCurrentLoggedInUser();

        UserDTO userDTO =
                modelMapper.map(
                        user,
                        UserDTO.class
                );

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User retrieved")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<Page<UserDTO>> getAllUsers(
            int page,
            int size
    ) {

        Page<User> users =
                userRepo.findAll(
                        PageRequest.of(page, size)
                );

        Page<UserDTO> userDTOS =
                users.map(user ->
                        modelMapper.map(
                                user,
                                UserDTO.class
                        ));

        return Response.<Page<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users retrieved")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<?> updatePassword(
            UpdatePasswordRequest updatePasswordRequest
    ) {

        User user = getCurrentLoggedInUser();

        String newPassword =
                updatePasswordRequest.getNewPassword();

        String oldPassword =
                updatePasswordRequest.getOldPassword();

        if (oldPassword == null || newPassword == null) {

            throw new BadRequestException(
                    "Old and New Password Required"
            );
        }

        // VALIDATE OLD PASSWORD
        if (!passwordEncoder.matches(
                oldPassword,
                user.getPassword()
        )) {

            throw new BadRequestException(
                    "Old Password not correct"
            );
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepo.save(user);

        // SEND PASSWORD CHANGE EMAIL
        Map<String, Object> templateVariables =
                new HashMap<>();

        templateVariables.put(
                "name",
                user.getFirstName()
        );

        NotificationDTO notificationDTO =
                NotificationDTO.builder()
                        .recipient(user.getEmail())
                        .subject(
                                "Your Password Was Successfully Changed"
                        )
                        .templateName("password-change")
                        .templateVariables(templateVariables)
                        .build();

        notificationService.sendEmail(
                notificationDTO,
                user
        );

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message(
                        "Password Changed Successfully"
                )
                .build();
    }

    @Override
    public Response<?> uploadProfilePicture(
            MultipartFile file
    ) {

        User user = getCurrentLoggedInUser();

        try {

            Path uploadPath = Paths.get(uploadDir);

            // CREATE FOLDER IF NOT EXISTS
            if (!Files.exists(uploadPath)) {

                Files.createDirectories(uploadPath);
            }

            // DELETE OLD PROFILE PICTURE
            if (user.getProfilePictureUrl() != null
                    && !user.getProfilePictureUrl().isEmpty()) {

                Path oldFile =
                        Paths.get(user.getProfilePictureUrl());

                if (Files.exists(oldFile)) {

                    Files.delete(oldFile);
                }
            }

            // GENERATE UNIQUE FILE NAME
            String originalFileName =
                    file.getOriginalFilename();

            String fileExtension =
                    originalFileName.substring(
                            originalFileName.lastIndexOf(".")
                    );

            String newFileName =
                    UUID.randomUUID() + fileExtension;

            Path filePath =
                    uploadPath.resolve(newFileName);

            // SAVE FILE
            Files.copy(
                    file.getInputStream(),
                    filePath
            );

            String fileUrl =
                    uploadDir + newFileName;

            user.setProfilePictureUrl(fileUrl);

            userRepo.save(user);

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message(
                            "Profile picture uploaded successfully."
                    )
                    .data(fileUrl)
                    .build();

        } catch (IOException e) {

            throw new RuntimeException(
                    e.getMessage()
            );
        }
    }
}