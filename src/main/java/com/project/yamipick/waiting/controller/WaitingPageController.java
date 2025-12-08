package com.project.yamipick.waiting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WaitingPageController {
    @GetMapping("/waitinguser")
    public String user() { return "/waiting/userwaiting"; } // userwaiting.html

    @GetMapping("/waitingstore")
    public String store() { return "/waiting/storewaiting"; } // storewaiting.html
    
    @GetMapping("/waitingstore/history")
    public String storeHistory() { return "/waiting/storewaitinghistory"; }
    
    @GetMapping("/waitinguser/history")
    public String userHistory() { return "/waiting/userwaitinghistory"; }
    
    @GetMapping("/waitingsearch")
    public String searchForm() {
        return "/waiting/waitingsearch"; // waitingsearch.html 파일을 보여줌
    }
    
    
}