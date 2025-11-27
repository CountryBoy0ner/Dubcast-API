package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<Playlist> findByScPlaylistUrl(String url);
}
