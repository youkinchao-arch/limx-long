package com.hongqiao.lims.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fails fast at startup when a weak/placeholder JWT secret is used while strong-secret
 * enforcement is enabled (set {@code lims.security.enforce-strong-secret=true} in production).
 */
@Component
public class SecretStartupValidator {

    private static final Logger log = LoggerFactory.getLogger(SecretStartupValidator.class);

    private final LimsProperties props;

    public SecretStartupValidator(LimsProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void validate() {
        String secret = props.getJwt().getSecret();
        boolean weak = secret == null || secret.isBlank() || secret.length() < 32
                || secret.startsWith("change-me");
        if (!weak) {
            return;
        }
        if (props.getSecurity().isEnforceStrongSecret()) {
            throw new IllegalStateException(
                    "Refusing to start: a strong SECRET_KEY (>= 32 chars, non-placeholder) is "
                            + "required when lims.security.enforce-strong-secret=true.");
        }
        log.warn("JWT secret looks weak/placeholder. Set a strong SECRET_KEY before production "
                + "and enable lims.security.enforce-strong-secret.");
    }
}
