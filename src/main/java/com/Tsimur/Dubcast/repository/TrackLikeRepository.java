package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.TrackLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackLikeRepository extends JpaRepository<TrackLike, Long> {

  boolean existsByUserIdAndTrackId(UUID userId, Long trackId);

  long countByTrackId(Long trackId);

  @Modifying
  @Query("delete from TrackLike tl where tl.userId = :userId and tl.trackId = :trackId")
  int deleteByUserIdAndTrackId(@Param("userId") UUID userId, @Param("trackId") Long trackId);

  @Modifying
  @Query(
      value =
          """
        INSERT INTO track_likes(user_id, track_id)
        VALUES (:userId, :trackId)
        ON CONFLICT (user_id, track_id) DO NOTHING
        """,
      nativeQuery = true)
  int insertIgnore(@Param("userId") UUID userId, @Param("trackId") Long trackId);
}
