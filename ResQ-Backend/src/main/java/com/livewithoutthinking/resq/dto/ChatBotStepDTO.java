package com.livewithoutthinking.resq.dto;

import lombok.Data;

import java.util.List;
@Data
public class ChatBotStepDTO {
    private String message;
    private List<String> suggestions;
    private boolean finalStep = false;
}

