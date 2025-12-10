package com.project.yamipick.ai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ai")
public class MenuRecommendPageController {
	
	@GetMapping("/recommend")
    public String menuRecommendPage() {
        return "ai/menu-recommend"; 
    }

}
