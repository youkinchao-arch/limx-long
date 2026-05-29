package com.hongqiao.lims.equipment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Append-only ledger of a calibration or maintenance event for a piece of equipment.
 * Each record carries the performer, result and an immutable e-signature hash
 * (ISO 17025 / 21 CFR Part 11 traceability). Records are never updated, only inserted.
 */
@Getter
@Setter
@Entity
@Table(name = "equipment_records")
public class EquipmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    /** calibration | maintenance */
    @Column(name = "record_type", nullable = false, length = 32)
    private String recordType;

    /** calibration: pass / limited / fail; maintenance: completed */
    @Column(length = 32)
    private String result;

    @Column(name = "performed_date")
    private LocalDate performedDate;

    @Column(name = "performed_by")
    private Long performedBy;

    @Column(name = "performed_by_name", length = 64)
    private String performedByName;

    /** External calibration agency / maintenance vendor. */
    @Column(length = 128)
    private String provider;

    @Column(name = "certificate_no", length = 128)
    private String certificateNo;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "cycle_days")
    private Integer cycleDays;

    @Column(name = "signature_hash", length = 128)
    private String signatureHash;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
