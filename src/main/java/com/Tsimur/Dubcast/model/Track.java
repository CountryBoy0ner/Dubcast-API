package com.Tsimur.Dubcast.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String scUrl;

    @Column(nullable = false, columnDefinition = "text")
    private String embedCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer durationSeconds;

    private String artworkUrl;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
