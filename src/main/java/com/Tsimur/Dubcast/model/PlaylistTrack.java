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

    /**
     * Какому плейлисту принадлежит эта строка.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    /**
     * Какой трек играет на этой позиции.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    /**
     * Порядок в плейлисте: 0,1,2...
     */
    @Column(nullable = false)
    private Integer position;

    /**
     * На будущее: начать не с 0-й секунды, а позже.
     * Можно оставить null.
     */
    @Column(name = "offset_seconds")
    private Integer offsetSeconds;

    /**
     * На будущее: играть не весь трек, а N секунд.
     * Можно оставить null => играем всю длину трека.
     */
    @Column(name = "custom_duration_seconds")
    private Integer customDurationSeconds;
}
