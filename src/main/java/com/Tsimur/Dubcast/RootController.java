package com.Tsimur.Dubcast;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {
    @GetMapping("/")
    public String player() {
        return "player"; // templates/player.html
    }
}
