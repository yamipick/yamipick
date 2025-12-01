package com.project.yamipick.waiting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/user")
    public String user() { return "userwaiting"; } // userwaiting.html

    @GetMapping("/store")
    public String store() { return "storewaiting"; } // storewaiting.html
}