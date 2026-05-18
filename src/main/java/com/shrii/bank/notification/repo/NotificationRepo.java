package com.shrii.bank.notification.repo;

import com.shrii.bank.notification.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<Notification, Long> {

}