package com.Tsimur.Dubcast.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageWEBRequest {
  @NotBlank private String text;
}
