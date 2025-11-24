package com.Tsimur.Dubcast.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackScheduleNowRequest {

    @NotNull
    private Long trackId;
}
