package com.project.yamipick.waiting.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import com.project.yamipick.waiting.repository.WaitingStoreRepository;
import com.project.yamipick.user.repository.UserRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.waiting.domain.WaitingStore;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WaitingPageController {

    private final WaitingStoreRepository waitingStoreRepository;
    private final UserRepository userRepository;

    // 1. 손님용 페이지 (URL: /waitinguser)
    @GetMapping("/waitinguser")
    public String user(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUserId(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("userId", user.getSeqUser());
            }
        }
        // 원래 경로: templates/waiting/userwaiting.html
        return "waiting/userwaiting"; 
    }

    // 2. 점주용 페이지 (URL: /waitingstore)
    @GetMapping("/waitingstore")
    public String store(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            String userId = auth.getName();
            User user = userRepository.findByUserId(userId).orElse(null);
            
            if (user != null) {
                // 내 seqUser를 ownerId로 가진 가게 찾기
                WaitingStore store = waitingStoreRepository.findByOwnerId(user.getSeqUser()).orElse(null);
                if (store != null) {
                    model.addAttribute("storeId", store.getId());
                }
            }
        }
        // 원래 경로: templates/waiting/storewaiting.html
        return "waiting/storewaiting";
    }

    // 3. 점주용 히스토리 (URL: /waitingstore/history)
    @GetMapping("/waitingstore/history")
    public String storeHistory(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUserId(auth.getName()).orElse(null);
            if (user != null) {
                WaitingStore store = waitingStoreRepository.findByOwnerId(user.getSeqUser()).orElse(null);
                if (store != null) {
                    model.addAttribute("storeId", store.getId());
                }
            }
        }
        // 원래 경로: templates/waiting/storewaitinghistory.html
        return "waiting/storewaitinghistory";
    }

    // 4. 손님용 히스토리 (URL: /waitinguser/history)
    @GetMapping("/waitinguser/history")
    public String userHistory(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByUserId(auth.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("userId", user.getSeqUser());
            }
        }
        // 원래 경로: templates/waiting/userwaitinghistory.html
        return "waiting/userwaitinghistory";
    }

    // 5. 매장 검색 (URL: /waitingsearch)
    @GetMapping("/waitingsearch")
    public String searchForm() {
        // 원래 경로: templates/waiting/waitingsearch.html
        return "waiting/waitingsearch";
    }
}