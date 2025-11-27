package com.Tsimur.Dubcast.controller.web;

import com.Tsimur.Dubcast.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Controller
public class IndexController {
    private final ScheduleEntryService scheduleEntryService;

    @GetMapping("/")
    public String index(Model model) {
        OffsetDateTime now = OffsetDateTime.now();

        var currentOpt = scheduleEntryService.getCurrent(now);

        currentOpt.ifPresent(c -> model.addAttribute("current", c));
        return "index";
    }

    @GetMapping("/profile")
    public String profile() {
        throw new RuntimeException("test 500");
    }

}
