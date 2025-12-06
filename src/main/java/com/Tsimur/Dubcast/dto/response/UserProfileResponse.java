package com.Tsimur.Dubcast.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class UserProfileResponse {

    private String username;
    private String bio;
}
