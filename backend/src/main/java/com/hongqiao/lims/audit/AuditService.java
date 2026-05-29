package com.hongqiao.lims.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongqiao.lims.common.BaseEntity;
import com.hongqiao.lims.user.User;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Records immutable audit entries for write/security operations. Never breaks the caller. */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /** Record a data-change action, resolving the actor from the security context. */
    public void recordChange(String action, String entityType, Object before, Object after) {
        User actor = currentUserOrNull();
        Long actorId = actor != null ? actor.getId() : null;
        String actorName = actor != null ? actor.getUsername() : null;
        save(action, entityType, entityId(after != null ? after : before), actorId, actorName,
                serialize(before), serialize(after));
    }

    /** Record a security/authentication action with an explicit actor (may be unauthenticated). */
    public void recordAuth(String action, Long actorId, String actorUsername) {
        save(action, "User", actorId != null ? String.valueOf(actorId) : null,
                actorId, actorUsername, null, null);
    }

    public void save(
            String action,
            String entityType,
            String entityId,
            Long actorId,
            String actorUsername,
            String before,
            String after) {
        try {
            AuditLog entry = new AuditLog();
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setActorId(actorId);
            entry.setActorUsername(actorUsername);
            entry.setBeforeValue(before);
            entry.setAfterValue(after);
            entry.setIp(currentIp());
            entry.setCreatedAt(Instant.now());
            repository.save(entry);
        } catch (RuntimeException ex) {
            // Auditing must never break the primary operation.
            log.warn("Failed to write audit log for action {} {}: {}", action, entityType,
                    ex.getMessage());
        }
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private String entityId(Object value) {
        if (value instanceof BaseEntity be && be.getId() != null) {
            return String.valueOf(be.getId());
        }
        return null;
    }

    private User currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return null;
    }
}
