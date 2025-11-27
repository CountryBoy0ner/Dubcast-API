package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

    List<PlaylistTrack> findByPlaylistIdOrderByPositionAsc(Long playlistId);


    @Query("""
            select max(pt.position)
            from PlaylistTrack pt
            where pt.playlist.id = :playlistId
            """)
    Integer findMaxPositionByPlaylistId(@Param("playlistId") Long playlistId);
}
