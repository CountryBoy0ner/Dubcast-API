package com.Tsimur.Dubcast.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @Deprecated
    @Column(name = "embed_code", columnDefinition = "TEXT")
    private String embedCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer durationSeconds;

    private String artworkUrl;

    @CreationTimestamp
    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
