package com.hongqiao.lims.personnel;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "training_records")
public class TrainingRecord extends BaseEntity {

    @Column(name = "personnel_id", nullable = false)
    private Long personnelId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 64)
    private String category;

    @Column(name = "train_date")
    private LocalDate trainDate;

    @Column(length = 64)
    private String result;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "text")
    private String remark;
}
