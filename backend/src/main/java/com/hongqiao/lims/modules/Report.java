package com.hongqiao.lims.modules;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reports")
public class Report extends BaseEntity {

    @Column(name = "report_no", nullable = false, unique = true, length = 64)
    private String reportNo;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 128)
    private String customer;

    @Column(length = 64)
    private String template;

    @Column(nullable = false, length = 32)
    private String status = "draft";

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(columnDefinition = "text")
    private String conclusion;

    @Column(columnDefinition = "text")
    private String remark;

    @Column(name = "submitted_by")
    private Long submittedBy;

    @Column(name = "submitted_by_name", length = 64)
    private String submittedByName;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_by_name", length = 64)
    private String approvedByName;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "signed_by")
    private Long signedBy;

    @Column(name = "signed_by_name", length = 64)
    private String signedByName;

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "signature_hash", length = 128)
    private String signatureHash;

    @Column(name = "issued_by")
    private Long issuedBy;

    @Column(name = "issued_by_name", length = 64)
    private String issuedByName;

    @Column(name = "issued_at")
    private Instant issuedAt;
}
