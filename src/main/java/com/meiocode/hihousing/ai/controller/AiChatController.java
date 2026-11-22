package com.meiocode.hihousing.ai.controller;

import org.springframework.web.bind.annotation.RestController;

import com.meiocode.hihousing.ai.service.AiChat;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class AiChatController {

    @Resource
    private AiChat aiChat;

    @GetMapping("/chat")
    public String chat(String id, String prompt) {
        return aiChat.chat(id, prompt);
    }

}
