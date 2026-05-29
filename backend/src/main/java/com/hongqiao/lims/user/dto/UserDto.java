package com.hongqiao.lims.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hongqiao.lims.user.User;
import java.util.List;

public record UserDto(
        Long id,
        String username,
        @JsonProperty("full_name") String fullName,
        String email,
        @JsonProperty("is_active") boolean active,
        @JsonProperty("is_superuser") boolean superuser,
        List<RoleDto> roles,
        List<String> permissions) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.isActive(),
                user.isSuperuser(),
                user.getRoles().stream().map(RoleDto::from).toList(),
                user.effectivePermissions().stream().sorted().toList());
    }
}
