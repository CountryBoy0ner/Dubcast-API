package com.Tsimur.Dubcast.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
