package com.Tsimur.Dubcast.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "playlist_tracks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_playlist_track_position",
                        columnNames = {"playlist_id", "position"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;


    @Column(nullable = false)
    private Integer position;

}
