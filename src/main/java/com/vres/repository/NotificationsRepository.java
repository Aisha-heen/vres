package com.vres.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vres.entity.Notifications;

public interface NotificationsRepository extends JpaRepository<Notifications, Integer> {

}
