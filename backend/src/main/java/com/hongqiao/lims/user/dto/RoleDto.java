package com.hongqiao.lims.user.dto;

import com.hongqiao.lims.user.Role;
import java.util.List;

public record RoleDto(Long id, String code, String name, List<String> permissions) {

    public static RoleDto from(Role role) {
        return new RoleDto(role.getId(), role.getCode(), role.getName(), role.getPermissionList());
    }
}
