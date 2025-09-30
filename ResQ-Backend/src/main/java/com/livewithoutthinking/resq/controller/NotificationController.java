package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.dto.NotificationDTO;
import com.livewithoutthinking.resq.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public List<NotificationDTO> getNotificationsByUser(@PathVariable Integer userId) {
        return notificationService.getNotificationsByUserId(userId);
    }

    @PutMapping("/mark-as-read/{id}")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Integer id) {
        boolean updated = notificationService.markAsRead(id);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

}
