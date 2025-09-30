package com.livewithoutthinking.resq.repository;

import com.livewithoutthinking.resq.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Integer> {
    @Query("SELECT  nt FROM NotificationTemplate nt WHERE nt.NotiType = :type")
    NotificationTemplate findByNotiType(String type);
}
