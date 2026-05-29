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
@Table(name = "documents")
public class Document extends BaseEntity {

    @Column(name = "doc_no", nullable = false, unique = true, length = 64)
    private String docNo;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 64)
    private String category;

    @Column(length = 32)
    private String version;

    @Column(nullable = false, length = 32)
    private String status = "draft";

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(columnDefinition = "text")
    private String remark;
}
