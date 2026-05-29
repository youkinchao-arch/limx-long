package com.hongqiao.lims.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "full_name", nullable = false, length = 128)
    private String fullName;

    @Column(length = 128)
    private String email;

    @JsonIgnore
    @Column(name = "hashed_password", nullable = false, length = 255)
    private String hashedPassword;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_superuser", nullable = false)
    private boolean superuser = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    /** Effective permission set; superusers implicitly hold the "*" wildcard. */
    public Set<String> effectivePermissions() {
        if (superuser) {
            return Set.of("*");
        }
        Set<String> perms = new HashSet<>();
        for (Role role : roles) {
            perms.addAll(role.getPermissionList());
        }
        return perms;
    }
}
