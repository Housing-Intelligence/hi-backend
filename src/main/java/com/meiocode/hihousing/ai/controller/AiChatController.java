package com.meiocode.hihousing.ai.controller;

import org.springframework.web.bind.annotation.RestController;

import com.meiocode.hihousing.ai.service.AiChat;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
public class AiChatController {
    
    private final AiChat aiChat;

    @GetMapping("/chat")
    public String chat(String id, String prompt) {
        return aiChat.chat(id, prompt);
    }
}
