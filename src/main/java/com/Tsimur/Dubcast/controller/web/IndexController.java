package com.Tsimur.Dubcast.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "index";
    }


    @GetMapping("/profile")
    public String profile() {
        throw new RuntimeException("test 500");
    }

}
