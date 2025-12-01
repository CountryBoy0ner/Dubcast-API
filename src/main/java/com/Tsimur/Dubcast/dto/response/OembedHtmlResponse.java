package com.Tsimur.Dubcast.dto.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class OembedHtmlResponse {
    private String code;

    public OembedHtmlResponse(String s) {
        this.code = s;
    }
}
