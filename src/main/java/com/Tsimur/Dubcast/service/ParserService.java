package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.TrackDto;
import java.util.List;

public interface ParserService {
  TrackDto parseTracksByUrl(String url);

  Integer getDurationSecondsByUrl(String url);

  List<TrackDto> parsePlaylistByUrl(String playlistUrl);

  String fetchOEmbedHtml(String soundcloudUrl);
}
