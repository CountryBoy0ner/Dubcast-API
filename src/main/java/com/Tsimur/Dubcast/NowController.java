package com.Tsimur.Dubcast;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NowController {
    private final NowPlayingService service;
    public NowController(NowPlayingService service) { this.service = service; }

    @GetMapping("/api/now")
    public NowPlayingDto now() { return service.now(); }
}
