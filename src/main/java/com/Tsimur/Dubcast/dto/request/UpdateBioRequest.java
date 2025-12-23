package com.Tsimur.Dubcast.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBioRequest {

  @Size(max = 512, message = "Bio must be at most 512 characters")
  private String bio;
}
