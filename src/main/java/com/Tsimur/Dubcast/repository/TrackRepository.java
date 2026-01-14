package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.Track;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackRepository extends JpaRepository<Track, Long> {

  boolean existsByScUrl(String scUrl);

  Optional<Track> findByScUrl(String scUrl);

  @Query("select t.likesCount from Track t where t.id = :trackId")
  Optional<Long> findLikesCount(@Param("trackId") Long trackId);
}
