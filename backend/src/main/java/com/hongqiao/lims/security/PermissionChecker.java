package com.hongqiao.lims.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** SpEL helper used in @PreAuthorize, e.g. @PreAuthorize("@perm.has('equipment:read')"). */
@Component("perm")
public class PermissionChecker {

    public boolean has(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("*") || a.getAuthority().equals(permission));
    }
}
