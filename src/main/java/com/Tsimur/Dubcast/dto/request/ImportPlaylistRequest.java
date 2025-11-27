package com.Tsimur.Dubcast.dto.request;

import lombok.Data;

@Data
public class ImportPlaylistRequest {

    // ссылка на твой плейлист в SC
    private String playlistUrl;

    // true = add to playlist to play after
    // false = start playing instantly
    private boolean appendAfterExisting = true; //default - true
}
