package com.hongqiao.lims.security;

import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }
}
