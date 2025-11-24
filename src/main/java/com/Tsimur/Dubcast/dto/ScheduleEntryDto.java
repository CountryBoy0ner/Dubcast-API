package com.Tsimur.Dubcast.dto;


import lombok.*;
import java.time.Instant;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntryDto {


    private Long id;

    @NotNull
    private TrackDto track;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

}
