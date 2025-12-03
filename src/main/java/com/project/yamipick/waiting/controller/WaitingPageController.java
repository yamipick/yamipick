package com.project.yamipick.waiting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WaitingPageController {
    @GetMapping("/waitinguser")
    public String user() { return "userwaiting"; } // userwaiting.html

    @GetMapping("/waitingstore")
    public String store() { return "storewaiting"; } // storewaiting.html
    
    @GetMapping("/waitingstore/history")
    public String storeHistory() { return "storewaitinghistory"; }
    
    @GetMapping("/waitinguser/history")
    public String userHistory() { return "userwaitinghistory"; }
}