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
        var nextOpt = scheduleEntryService.getNext(now);

        currentOpt.ifPresent(c -> model.addAttribute("current", c));
        nextOpt.ifPresent(n -> model.addAttribute("next", n));

        return "index";
    }

    @GetMapping("/profile")
    public String profile() {
        throw new RuntimeException("test 500");
    }

}
