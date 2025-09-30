package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.dto.NotificationTemplateCreateDTO;
import com.livewithoutthinking.resq.dto.NotificationTemplateDTO;
import com.livewithoutthinking.resq.dto.UserDto;
import com.livewithoutthinking.resq.entity.NotificationTemplate;
import com.livewithoutthinking.resq.entity.User;
import com.livewithoutthinking.resq.helpers.ApiResponse;
import com.livewithoutthinking.resq.repository.NotificationTemplateRepository;
import com.livewithoutthinking.resq.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/resq/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateRepository notificationTemplateRepository;
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<NotificationTemplateDTO>> getAllTemplates() {
        List<NotificationTemplateDTO> templates = notificationTemplateRepository.findAll()
                .stream()
                .map(template -> {
                    NotificationTemplateDTO dto = new NotificationTemplateDTO();
                    dto.setNotificationTemplateID(template.getNotificationTemplateID());
                    dto.setTitle(template.getTitle());
                    dto.setNotitype(template.getNotiType());
                    dto.setCreatedAt(template.getCreatedAt());
                    dto.setUpdatedAt(template.getUpdatedAt());
                    return dto;
                }).toList();

        return ResponseEntity.ok(templates);
    }

    @PostMapping
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody NotificationTemplateCreateDTO dto) {
        NotificationTemplate template = new NotificationTemplate();
        template.setNotiType(dto.getNotiType());
        template.setTitle(dto.getTitle());
        template.setCreatedAt(new Date());
        template.setUpdatedAt(new Date());

        NotificationTemplate saved = notificationTemplateRepository.save(template);
        return ResponseEntity.status(201).body(saved); // Created
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @PathVariable int id,
            @RequestBody NotificationTemplateCreateDTO dto
    ) {
        NotificationTemplate template = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        template.setNotiType(dto.getNotiType());
        template.setTitle(dto.getTitle());
        template.setUpdatedAt(new Date());

        NotificationTemplate updated = notificationTemplateRepository.save(template);
        return ResponseEntity.ok(updated);
    }


}
