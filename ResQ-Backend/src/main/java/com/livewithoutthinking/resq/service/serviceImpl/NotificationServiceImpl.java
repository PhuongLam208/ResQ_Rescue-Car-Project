package com.livewithoutthinking.resq.service.serviceImpl;

import com.livewithoutthinking.resq.dto.NotificationDTO;
import com.livewithoutthinking.resq.entity.Notification;
import com.livewithoutthinking.resq.entity.NotificationTemplate;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.repository.NotificationRepository;
import com.livewithoutthinking.resq.repository.NotificationTemplateRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import com.livewithoutthinking.resq.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Override
    public List<NotificationDTO> getNotificationsByUserId(Integer userId) {
        List<Notification> notifications = notificationRepository.findByUser_UserId(userId);
        return notifications.stream().map(n -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setNoId(n.getNoId());
            dto.setMessage(n.getMessage());
            dto.setViewStatus(n.getViewStatus());
            dto.setCreatedAt(n.getCreatedAt());
            dto.setUpdatedAt(n.getUpdatedAt());
            dto.setIsRead(n.getIsRead());

            if (n.getNotificationTemplate() != null) {
                dto.setNotiType(n.getNotificationTemplate().getNotiType());
                dto.setTitle(n.getNotificationTemplate().getTitle());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean markAsRead(Integer notificationId) {
        Optional<Notification> optional = notificationRepository.findById(notificationId);
        if (optional.isEmpty()) return false;

        Notification notification = optional.get();
        notification.setIsRead(true);
        notification.setUpdatedAt(new Date());
        notificationRepository.save(notification);
        return true;
    }

    public void notifyUser(Integer userId, String title, String message, String viewStatus) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Notification notification = new Notification();
            notification.setUser(user.get());
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setViewStatus(viewStatus);
            notification.setCreatedAt(new Date());
            notification.setUpdatedAt(new Date());
            notification.setIsRead(false); // chưa đọc
            notificationRepository.save(notification);
        }
    }

    @Override
    public NotificationTemplate findByNotiType(String notiType) {
        return notificationTemplateRepository.findByNotiType(notiType);
    }

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

}
