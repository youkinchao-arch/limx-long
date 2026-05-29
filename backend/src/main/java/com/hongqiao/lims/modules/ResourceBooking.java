package com.hongqiao.lims.modules;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "resource_bookings")
public class ResourceBooking extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "resource_type", nullable = false, length = 32)
    private String resourceType;

    @Column(name = "resource_name", length = 128)
    private String resourceName;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(nullable = false, length = 32)
    private String status = "pending";

    @Column(columnDefinition = "text")
    private String remark;
}
