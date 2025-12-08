package com.Tsimur.Dubcast.analytics.dto;


import lombok.Data;

@Data
public class HeartbeatMessage {
    private String page;      // например: "/radio", "/chat"
    private Long trackId;     // id текущего трека (может быть null)
}
