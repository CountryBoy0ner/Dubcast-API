package com.Tsimur.Dubcast.controller.web;

import com.Tsimur.Dubcast.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final ScheduleEntryService scheduleEntryService;

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        OffsetDateTime now = OffsetDateTime.now();

        var currentOpt = scheduleEntryService.getCurrent(now);
        currentOpt.ifPresent(c -> model.addAttribute("current", c));

        boolean authenticated = (principal != null);
        model.addAttribute("authenticated", authenticated);
        if (authenticated) {
            model.addAttribute("principalName", principal.getName());
        }

        return "index"; // твой шаблон с радио + чат
    }
}
