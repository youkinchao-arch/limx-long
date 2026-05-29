package com.hongqiao.lims.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record UserUpdateRequest(
        @JsonProperty("full_name") String fullName,
        String email,
        String password,
        @JsonProperty("is_active") Boolean active,
        @JsonProperty("role_ids") List<Long> roleIds) {
}
