package com.Tsimur.Dubcast.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UrlRequest {
    @NotBlank
    String url;
}
