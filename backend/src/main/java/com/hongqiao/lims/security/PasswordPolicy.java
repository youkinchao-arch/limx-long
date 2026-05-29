package com.hongqiao.lims.security;

import com.hongqiao.lims.common.ApiException;
import com.hongqiao.lims.config.LimsProperties;
import org.springframework.stereotype.Component;

/** Enforces the minimum password strength rules on create / change-password flows. */
@Component
public class PasswordPolicy {

    private final int minLength;

    public PasswordPolicy(LimsProperties props) {
        this.minLength = props.getSecurity().getPasswordMinLength();
    }

    public void validate(String password) {
        if (password == null || password.length() < minLength) {
            throw ApiException.badRequest(
                    "密码至少需要 " + minLength + " 位 / Password must be at least " + minLength
                            + " characters");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw ApiException.badRequest(
                    "密码必须同时包含字母和数字 / Password must contain both letters and digits");
        }
    }
}
