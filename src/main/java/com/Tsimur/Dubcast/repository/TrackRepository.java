package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    boolean existsByScUrl(String scUrl);
    Optional<Track> findByScUrl(String scUrl);      // <-- вот этот

}