package com.hongqiao.lims.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 255)
    private String description;

    /** Comma-separated permission codes; "*" means all permissions. */
    @Column(nullable = false, length = 2000)
    private String permissions = "";

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    public List<String> getPermissionList() {
        if (permissions == null || permissions.isBlank()) {
            return List.of();
        }
        return Arrays.stream(permissions.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
