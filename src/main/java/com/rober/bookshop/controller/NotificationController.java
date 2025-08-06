package com.rober.bookshop.controller;

import com.rober.bookshop.model.request.NotificationRequestDTO;
import com.rober.bookshop.service.impl.FirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final FirebaseMessagingService firebaseMessagingService;

    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotificationByToken(@RequestBody NotificationRequestDTO request) {
        return ResponseEntity.ok(firebaseMessagingService.sendNotificationByToken(request));
    }

}
