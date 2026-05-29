package com.hongqiao.lims.equipment;

import com.hongqiao.lims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "equipment")
public class Equipment extends BaseEntity {

    @Column(name = "asset_no", nullable = false, unique = true, length = 64)
    private String assetNo;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 64)
    private String category;

    @Column(length = 128)
    private String model;

    @Column(length = 128)
    private String manufacturer;

    @Column(name = "serial_no", length = 128)
    private String serialNo;

    @Column(length = 128)
    private String location;

    @Column(nullable = false, length = 32)
    private String status = "idle";

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "calibration_date")
    private LocalDate calibrationDate;

    @Column(name = "calibration_due")
    private LocalDate calibrationDue;

    @Column(name = "calibration_cycle_days")
    private Integer calibrationCycleDays;

    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    @Column(name = "maintenance_due")
    private LocalDate maintenanceDue;

    @Column(name = "maintenance_cycle_days")
    private Integer maintenanceCycleDays;

    @Column(columnDefinition = "text")
    private String remark;

    /** Derived calibration freshness: overdue / due_soon / valid / unknown. */
    @Transient
    public String getCalibrationStatus() {
        return dueStatus(calibrationDue);
    }

    /** Derived maintenance freshness: overdue / due_soon / valid / unknown. */
    @Transient
    public String getMaintenanceStatus() {
        return dueStatus(maintenanceDue);
    }

    private static String dueStatus(LocalDate due) {
        if (due == null) {
            return "unknown";
        }
        LocalDate today = LocalDate.now();
        if (due.isBefore(today)) {
            return "overdue";
        }
        if (!due.isAfter(today.plusDays(30))) {
            return "due_soon";
        }
        return "valid";
    }
}
