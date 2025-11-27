package com.Tsimur.Dubcast.dto.response;

import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlaylistScheduleResponse {
    private PlaylistDto playlist;
    private List<ScheduleEntryDto> scheduleEntries;
}
