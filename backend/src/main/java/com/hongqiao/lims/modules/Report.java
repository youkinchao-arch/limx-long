package com.hongqiao.lims.modules;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
    private String remark;
}
