package com.project.yamipick.ai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.ai.dto.ChatRequest;
import com.project.yamipick.ai.dto.ChatResponse;
import com.project.yamipick.ai.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/api/chat/send")
    public ChatResponse createChatResponse(@RequestBody ChatRequest req) {
        return chatService.processChat(req);
    }
}

