package com.hongqiao.lims.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UserCreateRequest(
        @NotBlank String username,
        @JsonProperty("full_name") @NotBlank String fullName,
        @NotBlank String password,
        String email,
        @JsonProperty("role_ids") List<Long> roleIds,
        @JsonProperty("is_superuser") boolean superuser) {
}
