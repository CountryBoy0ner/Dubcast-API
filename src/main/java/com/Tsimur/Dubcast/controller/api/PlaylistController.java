package com.Tsimur.Dubcast.controller.api;


import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.request.UrlRequest;
import com.Tsimur.Dubcast.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public ResponseEntity<List<PlaylistDto>> getAll() {
        return ResponseEntity.ok(playlistService.getAll());
    }

    // один плейлист по id
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.getById(id));
    }

    // удалить плейлист + все его PlaylistTrack (через каскад)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playlistService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/import")
    public ResponseEntity<PlaylistDto> importPlaylist(@RequestBody @Valid UrlRequest request) {
        PlaylistDto playlist = playlistService.importPlaylistFromUrl(request.getUrl());
        return ResponseEntity.ok(playlist);
    }
}
