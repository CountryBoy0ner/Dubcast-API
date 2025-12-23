package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.Track;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {

  boolean existsByScUrl(String scUrl);

  Optional<Track> findByScUrl(String scUrl); // <-- вот этот
}
