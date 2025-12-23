package com.Tsimur.Dubcast.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id @UuidGenerator private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(name = "bio", length = 512)
  private String bio;

  @Column(name = "username", length = 50, unique = true)
  private String username;

  @CreationTimestamp
  @Setter(AccessLevel.NONE)
  @Column(nullable = false)
  private OffsetDateTime createdAt;
}
