package com.livewithoutthinking.resq.controller;

import com.livewithoutthinking.resq.dto.ChatBotStepDTO;
import com.livewithoutthinking.resq.dto.ConversationDTO;
import com.livewithoutthinking.resq.repository.StaffRepository;
import com.livewithoutthinking.resq.repository.UserRepository;
import com.livewithoutthinking.resq.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    @PostMapping("/next")
    public ChatBotStepDTO getNextStep(@RequestBody Map<String, String> payload) {
        String userInput = payload.get("message").toLowerCase();
        Integer userId = Integer.parseInt(payload.get("userId"));
        Integer staffUserId = payload.containsKey("staffUserId")
                ? Integer.parseInt(payload.get("staffUserId"))
                : 1;

        ChatBotStepDTO step = new ChatBotStepDTO();

        switch (userInput) {
            case "hello":
            case "start":
                step.setMessage("Hello . Here are some common support topics:");
                step.setSuggestions(List.of(
                        "Update my ResQ account information",
                        "Check rescue request status",
                        "Information about rescue services",
                        "Other issues"
                ));
                break;

            case "update my resq account information":
                step.setMessage("Which information would you like to update?");
                step.setSuggestions(List.of("Update email", "Change phone number", "Update avatar", "Go back"));
                break;

            case "update email":
            case "change phone number":
            case "update avatar":
                step.setMessage("Your request \"" + userInput + "\" has been recorded.\nAre you satisfied with this response?");
                step.setSuggestions(List.of("Satisfied", "Not satisfied"));
                break;

            case "check rescue request status":
                step.setMessage("Please go to the 'Recent Requests' section to track your request status.\nAre you satisfied with this response?");
                step.setSuggestions(List.of("Satisfied", "Not satisfied"));
                break;

            case "information about rescue services":
                step.setMessage("ResQ currently provides the following services:\n On-site Repair\n Towing\n Substitute Driver.\nWhich service would you like to know more about?");
                step.setSuggestions(List.of("On-site Repair", "Towing", "Substitute Driver", "Go back"));
                break;

            case "on-site repair":
            case "towing":
            case "substitute driver":
                step.setMessage("Noted. The service \"" + userInput + "\" is available 24/7 within the city.\nAre you satisfied with this response?");
                step.setSuggestions(List.of("Satisfied", "Not satisfied"));
                break;

            case "other issues":
            case "not satisfied":
                step.setMessage("Please describe your issue below. Our staff will assist you shortly.");
                step.setSuggestions(List.of());
                step.setFinalStep(true);
                createRealConversation(userId, staffUserId);
                break;

            case "satisfied":
                step.setMessage("Thank you for using ResQ! If you need further support, type 'Start' to begin again.");
                step.setSuggestions(List.of("Start"));
                step.setFinalStep(true);
                break;

            case "go back":
                step.setMessage("Which topic would you like support with?");
                step.setSuggestions(List.of(
                        "Update my ResQ account information",
                        "Check rescue request status",
                        "Information about rescue services",
                        "Other issues"
                ));
                break;

            default:
                step.setMessage("Sorry, ResQ didn't understand your request. Please choose one of the suggestions or select 'Other issues' to chat with our staff.");
                step.setSuggestions(List.of("Start", "Other issues"));
                break;
        }

        return step;
    }

    public void createRealConversation(Integer userId, Integer staffUserId) {
        List<ConversationDTO> existing = messageService.getAllConversationsDTOByUserId(userId);

        //  Nếu đã có cuộc hội thoại mở (dù là với staff nào) → không tạo mới
        boolean hasOpenConversation = existing.stream()
                .anyMatch(conv -> Boolean.FALSE.equals(conv.getIsClosed()));

        if (hasOpenConversation) {
            return; // Đã có 1 cuộc hội thoại chưa đóng → không tạo mới
        }


        // sửa thành sendMessageForceNewConversation
        //  Nếu tất cả hội thoại đã đóng, hoặc chưa từng có → tạo hội thoại mới với staffUserId
        messageService.sendMessageForceNewConversation(
                userId,            // customerId
                staffUserId,       // staffUserId
                staffUserId,       // senderId (người gửi đầu tiên là staff)
                "Cảm ơn bạn. Nhân viên chăm sóc khách hàng sẽ hỗ trợ bạn ngay sau đây." // nội dung
        );

        System.out.println("Tạo conversation mới với staffId = " + staffUserId + " cho userId = " + userId);
    }
}
