package com.Tsimur.Dubcast.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User sender;

  @Column(name = "text", nullable = false, length = 1000)
  private String text;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
