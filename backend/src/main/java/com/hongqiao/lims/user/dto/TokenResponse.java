package com.hongqiao.lims.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType) {

    public static TokenResponse bearer(String token) {
        return new TokenResponse(token, "bearer");
    }
}
