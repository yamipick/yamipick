package com.project.yamipick.waiting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.waiting.domain.Waiting;
import com.project.yamipick.waiting.service.WaitingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserWaitingController {

    private final WaitingService waitingService;

    @PostMapping("/waiting/register")
    public Waiting register(@RequestParam("userId") Long userId, @RequestParam("size") int size) {
        return waitingService.register(userId, size);
    }

    @GetMapping("/waiting/check/{id}")
    public long check(@PathVariable("id") Long id) {
        return waitingService.getCountAhead(id);
    }

    @PostMapping("/waiting/cancel/{id}")
    public String cancel(@PathVariable("id") Long id) {
        waitingService.cancel(id);
        return "ok";
    }
}