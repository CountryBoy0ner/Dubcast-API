package com.Tsimur.Dubcast.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageIncomingDto {
    @NotBlank
    private String text;
}

