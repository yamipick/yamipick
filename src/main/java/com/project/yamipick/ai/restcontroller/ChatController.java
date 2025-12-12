package com.project.yamipick.ai.restcontroller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.ai.dto.ChatRequest;
import com.project.yamipick.ai.dto.ChatResponse;
import com.project.yamipick.ai.service.ChatService;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;


	@PostMapping("/api/chat/send")
	public ChatResponse createChatResponse(
	        @RequestBody ChatRequest req,
	        @AuthenticationPrincipal UserDetails user
	) {
	    if (user == null) {
	        throw new IllegalStateException("로그인이 필요한 서비스입니다.");
	}
	
	String userId = user.getUsername();
	
	Long seqUser = userRepository.findByUserId(userId)
	        .orElseThrow(() -> new IllegalStateException("사용자 정보 없음"))
	            .getSeqUser();
	
	    return chatService.processChat(req, seqUser);
	}
}

