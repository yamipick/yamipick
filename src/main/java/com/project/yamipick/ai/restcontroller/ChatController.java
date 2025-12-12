package com.project.yamipick.ai.restcontroller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.ai.dto.ChatRequest;
import com.project.yamipick.ai.dto.ChatResponse;
import com.project.yamipick.ai.service.ChatService;
import com.project.yamipick.security.auth.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/api/chat/send")
    public ChatResponse createChatResponse(
    		@RequestBody ChatRequest req,
    		@AuthenticationPrincipal CustomUserDetails user
    		
    ) {
        Long seqUser = user.getSeqUser();
    	return chatService.processChat(req, seqUser);
    }
}

