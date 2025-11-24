package com.Tsimur.Dubcast.integration.soundcloud;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoundcloudOEmbedResponse {

    private String title;
    private String author_name;
    private String author_url;
    private String provider_name;
    private String provider_url;
    private String thumbnail_url;
    private String html;
}
