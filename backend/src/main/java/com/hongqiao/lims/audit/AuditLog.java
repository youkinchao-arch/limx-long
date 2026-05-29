package com.hongqiao.lims.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Append-only audit record (ALCOA+): who did what to which record, when, with before/after values.
 * Has no updated_at and is never mutated after creation.
 */
@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_username", length = 64)
    private String actorUsername;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(name = "entity_type", length = 64)
    private String entityType;

    @Column(name = "entity_id", length = 64)
    private String entityId;

    @JsonProperty("before")
    @Column(name = "before_value", columnDefinition = "text")
    private String beforeValue;

    @JsonProperty("after")
    @Column(name = "after_value", columnDefinition = "text")
    private String afterValue;

    @Column(length = 64)
    private String ip;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
