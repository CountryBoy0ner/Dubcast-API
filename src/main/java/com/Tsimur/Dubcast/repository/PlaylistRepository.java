package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.Playlist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
  Optional<Playlist> findByScPlaylistUrl(String url);
}
