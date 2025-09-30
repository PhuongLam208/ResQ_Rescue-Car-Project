package com.livewithoutthinking.resq.config;

import com.livewithoutthinking.resq.dto.RescueRequestAcceptedDto;
import com.livewithoutthinking.resq.dto.RescueRequestNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RescueRequestSender {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendRescueRequestToPartner(int partnerId, RescueRequestNotificationDto dto) {
        messagingTemplate.convertAndSend("/topic/rescue/partner/" + partnerId, dto);
    }

    public void sendRescueRequestToPartnerAccept(int rrid, RescueRequestAcceptedDto dto) {
        messagingTemplate.convertAndSend("/topic/rescue/accepted/" + rrid, dto);
    }

    public void sendMessageToUser(int rrid, String message) {
        messagingTemplate.convertAndSend("/topic/rescue/user/" + rrid, message);
    }

}