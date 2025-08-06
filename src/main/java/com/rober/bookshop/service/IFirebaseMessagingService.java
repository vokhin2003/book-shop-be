package com.rober.bookshop.service;

import com.rober.bookshop.model.request.NotificationRequestDTO;

public interface IFirebaseMessagingService {
    String sendNotificationByToken(NotificationRequestDTO request);
}
