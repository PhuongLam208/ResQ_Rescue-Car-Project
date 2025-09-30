package com.livewithoutthinking.resq.service;


import com.livewithoutthinking.resq.dto.NotificationDTO;
import com.livewithoutthinking.resq.entity.Notification;
import com.livewithoutthinking.resq.entity.NotificationTemplate;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public interface NotificationService {

    List<NotificationDTO> getNotificationsByUserId(Integer userId);
    boolean markAsRead(Integer notificationId);
    void notifyUser(Integer userId, String title, String message, String viewStatus);
    NotificationTemplate findByNotiType(String notiType);
    Notification save(Notification notification);
}
