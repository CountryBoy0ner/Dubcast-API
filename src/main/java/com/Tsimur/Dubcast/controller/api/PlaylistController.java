package com.Tsimur.Dubcast.controller.api;


import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.request.UrlRequest;
import com.Tsimur.Dubcast.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;


    @PostMapping("/import")
    public ResponseEntity<PlaylistDto> importPlaylist(@RequestBody @Valid UrlRequest request) {
        PlaylistDto playlist = playlistService.importPlaylistFromUrl(request.getUrl());
        return ResponseEntity.ok(playlist);
    }
}
