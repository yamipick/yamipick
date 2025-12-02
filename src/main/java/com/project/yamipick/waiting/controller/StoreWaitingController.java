package com.project.yamipick.waiting.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.waiting.domain.Store;
import com.project.yamipick.waiting.dto.WaitingDTO;
import com.project.yamipick.waiting.service.WaitingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StoreWaitingController {

    private final WaitingService waitingService;

    @GetMapping("/waiting/list")
    public List<WaitingDTO> list(@RequestParam(value = "storeId", defaultValue = "1") Long storeId) {
        return waitingService.getStoreList(storeId);
    }

    @GetMapping("/waiting/store-info")
    public Store storeInfo(@RequestParam(value = "storeId", defaultValue = "1") Long storeId) { return waitingService.getStoreInfo(storeId); }

    // ★ 수정: 서비스 메소드 이름(toggleWaitingOpen)과 일치시킴
    @PostMapping("/waiting/toggle")
    public boolean toggle(@RequestParam(value = "storeId", defaultValue = "1") Long storeId) {
        return waitingService.toggleWaitingOpen(storeId);
    }

    @PostMapping("/waiting/call/{id}")
    public String call(@PathVariable("id") Long id) { waitingService.call(id); return "ok"; }

    @PostMapping("/waiting/enter/{id}")
    public String enter(@PathVariable("id") Long id) { waitingService.enter(id); return "ok"; }

    @PostMapping("/waiting/reject/{id}")
    public String reject(@PathVariable("id") Long id) { waitingService.cancel(id, true); return "ok"; }

    @PostMapping("/waiting/notice")
    public String notice(@RequestParam("content") String content) { waitingService.notice(content); return "ok"; }
}