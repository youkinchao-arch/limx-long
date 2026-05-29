package com.hongqiao.lims.modules;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "environment_records")
public class EnvironmentRecord extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String location;

    @Column(precision = 6, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 6, scale = 2)
    private BigDecimal humidity;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Column(nullable = false, length = 32)
    private String status = "normal";

    @Column(columnDefinition = "text")
    private String remark;
}
