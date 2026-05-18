package com.shrii.bank.notification.services;

import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.notification.dtos.NotificationDTO;

public interface NotificationService {

    void sendEmail(NotificationDTO notificationDTO, User user);
}