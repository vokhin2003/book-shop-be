package com.rober.bookshop.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.rober.bookshop.model.request.NotificationRequestDTO;
import com.rober.bookshop.repository.UserDeviceTokenRepository;
import com.rober.bookshop.service.IFirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseMessagingService implements IFirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    @Override
    public String sendNotificationByToken(NotificationRequestDTO request) {
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .setImage(request.getImage())
                .build();

        Message message = Message.builder()
                .setToken(request.getDeviceToken())
                .setNotification(notification)
                .putAllData(request.getData())
                .build();
        try {
            firebaseMessaging.send(message);
            log.info("Successfully sent notification to token: {}", request.getDeviceToken());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to token: {}. Error: {}", request.getDeviceToken(), e.getMessage());
            if (e.getErrorCode().equals("messaging/registration-token-not-registered")) {
                userDeviceTokenRepository.findByDeviceToken(request.getDeviceToken())
                        .ifPresent(token -> {
                            token.setActive(false);
                            userDeviceTokenRepository.save(token);
                            log.info("Marked token as inactive: {}", request.getDeviceToken());
                        });
            }
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }

        return "Successfully sent notification";
    }

}
